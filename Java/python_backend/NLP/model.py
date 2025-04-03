from flask import Flask, request, send_file
from werkzeug.utils import secure_filename
import os
import whisper
import torchaudio
from moviepy import VideoFileClip
from pyannote.audio import Pipeline
from pyannote.audio.pipelines.utils.hook import ProgressHook
from datetime import timedelta
from transformers import pipeline
import zipfile

app = Flask(__name__)
UPLOAD_FOLDER = "uploads"
OUTPUT_FOLDER = "output"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
os.makedirs(OUTPUT_FOLDER, exist_ok=True)

whisper_model = whisper.load_model("medium")
# You need to provide an authentication token from HuggingFace
diarization_pipeline = Pipeline.from_pretrained(
    "pyannote/speaker-diarization-3.1",
    use_auth_token="YOUR_HUGGINGFACE_TOKEN")  # Replace with your token

# Load T5 model for summarization
summarizer = pipeline("summarization", model="t5-base")

def format_timestamp(seconds):
    milliseconds = int((seconds % 1) * 1000)
    formatted_time = str(timedelta(seconds=int(seconds))) + f",{milliseconds:03d}"
    return formatted_time  # Fixed: was returning the function itself

def extract_audio(video_path, audio_path):
    video = VideoFileClip(video_path)  # Fixed: was using VideoClip
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
        diarization = diarization_pipeline(audio_path, hook=hook)

    srt_output = []
    srt_index = 1

    transcript_text = ""  # Store full transcript for summarization

    for whisper_segment in result["segments"]:
        whisper_start, whisper_end = whisper_segment["start"], whisper_segment["end"]
        text = whisper_segment["text"]
        transcript_text += text + " "  # Collect transcript text

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

    # Generate summary
    summary = summarizer(transcript_text, max_length=350, min_length=80, do_sample=False)[0]["summary_text"]

    # Save summary
    summary_path = os.path.join(OUTPUT_FOLDER, filename.rsplit(".", 1)[0] + "_summary.txt")
    with open(summary_path, "w", encoding="utf-8") as f:
        f.write(summary)

    # 🔹 Zip all files together
    zip_path = os.path.join(OUTPUT_FOLDER, filename.rsplit(".", 1)[0] + "_output.zip")
    with zipfile.ZipFile(zip_path, "w") as zipf:
       zipf.write(srt_path, os.path.basename(srt_path))
       zipf.write(summary_path, os.path.basename(summary_path))


    return send_file(zip_path, as_attachment=True, download_name=filename.rsplit(".", 1)[0] + "_output.zip")



if __name__ == "__main__":
    app.run(debug=True, port=5001)