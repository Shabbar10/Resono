from flask import Flask, request, jsonify
import matplotlib.pyplot as plt
import os
from viewer_emotion_analyzer import ViewerEmotionAnalyzer
import numpy as np
import cv2

app = Flask(__name__)
analyzer = ViewerEmotionAnalyzer()

UPLOAD_FOLDER = "uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route("/", methods=["POST"])
def test():
    try:
        frame = request.data

        frame = np.frombuffer(frame, dtype=np.uint8)
        frame = cv2.imdecode(frame, cv2.IMREAD_COLOR)

        processed_frame, emotion = analyzer.analyze_viewer_emotion(frame)
        print(f"Emotion: {emotion}")

        return emotion, 200
    except Exception as e:
        print(e)
        return str(e), 500

if __name__ == "__main__":
    app.run(port=5000)