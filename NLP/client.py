import requests

url = "http://127.0.0.1:5000/process"
file_path = "still-listening-gentleman.wav"  # Change this to your video/audio file

with open(file_path, "rb") as f:
    files = {"file": (file_path, f, "multipart/form-data")}
    response = requests.post(url, files=files)

if response.status_code == 200:
    with open("output.srt", "wb") as f:
        f.write(response.content)
    print("✅ SRT saved as output.srt")
else:
    print("❌ Error:", response.json())
