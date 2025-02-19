from flask import Flask, request, send_file
from werkzeug.utils import secure_filename
import os
import whisper
import torchaudio
from moviepy.editor import VideoFileClip
from pyannote.audio import Pipeline
from pyannote.audio.pipelines.utils.hook import ProgressHook
from datetime import timedelta

app = Flask(__name__)
UPLOAD_FOLDER = "uploads"
OUTPUT_FOLDER = "output"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

whisper_model = whisper.load_model("medium")
pipeline = Pipeline.from_pretrained(
    "pyannote/speaker-diarization-3.1") # add use_auth_token

def format_timestamp(seconds):
    milliseconds = int((seconds % 1) * 1000)
    formatted_time = str(timedelta(seconds=int(seconds))) + f",{milliseconds:03d}"
    return format_timestamp

def extract_audio(video_path, audio_path):
    video = VideoClip(video_path)
    video.audio.write_audiofile(audio_path, codec="pcm_s16le", fps=16000)

@app.route("/process", methods=["POST"])
def process():
    if "file" not in request.files:
        return {"error": "No file uploaded"}, 400

    # Save file in uploads folder
    file = request.files["file"]
    filename = secure_filename(file.filename)
    filepath = os.path.join(UPLOAD_FOLDER, filename)
    file.save(filepath)

    # Convert video to audio, if needed
    if filename.endswith((".mp4", ".mkv", ".avi")):
        audio_path = os.path.join(UPLOAD_FOLDER, filename.rsplit(".", 1)[0] + ".wav")
        extract_audio(filepath, audio_path)
    else:
        audio_path = filepath

    # Run models
    result = whisper_model.transcribe(audio_path)
    with ProgressHook() as hook:
        diarization = pipeline(audio_path, hook=hook)

    srt_output = []
    srt_index = 1

    for whisper_segment in result["segments"]:
        whisper_start, whisper_end = whisper_segment["start"], whisper_segment["end"]
        text = whisper_segment["text"]

        # Find the best matching diarization segment
        speaker_label = "UNKNOWN"
        for turn, _, speaker in diarization.itertracks(yield_label=True):
            if turn.start <= whisper_end and turn.end >= whisper_start:
                speaker_label = speaker
                break  

        srt_output.append(f"{srt_index}")
        srt_output.append(f"{format_timestamp(whisper_start)} --> {format_timestamp(whisper_end)}")
        srt_output.append(f"{speaker_label}: {text}")
        srt_output.append("")
        srt_index += 1

    srt_path = os.path.join(OUTPUT_FOLDER, filename.rsplit(".", 1)[0] + ".srt")
    with open(srt_path, "w", encoding="utf-8") as f:
        f.write("\n".join(srt_output))

    return send_file(srt_path, as_attachment=True, download_name=filename.rsplit(".", 1)[0] + ".srt")

if __name__ == "__main__":
    app.run(debug=True, port=5000)