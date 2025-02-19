from flask import Flask, request, jsonify
from viewer_emotion_analyzer import ViewerEmotionAnalyzer
import cv2
import numpy as np
import base64

app = Flask(__name__)
analyzer = ViewerEmotionAnalyzer()

@app.route('/analyze', methods=['POST'])
def analyze():
    try:
        data = request.json
        genre = data.get('genre')
        frame_data = data.get('frame')  # Base64 encoded frame
        timestamp = data.get('timestamp')

        # Decode base64 frame
        frame_bytes = base64.b64decode(frame_data)
        frame_arr = np.frombuffer(frame_bytes, dtype=np.uint8)
        frame = cv2.imdecode(frame_arr, cv2.IMREAD_COLOR)

        # Analyze frame
        processed_frame, emotion = analyzer.analyze_viewer_emotion(frame, timestamp)
        
        # Encode processed frame back to base64
        _, buffer = cv2.imencode('.jpg', processed_frame)
        processed_frame_b64 = base64.b64encode(buffer).decode('utf-8')

        return jsonify({
            'processedFrame': processed_frame_b64,
            'emotion': emotion
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/generate-report', methods=['POST'])
def generate_report():
    try:
        data = request.json
        genre = data.get('genre')
        report = analyzer.generate_report(genre)
        return jsonify(report)
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(port=5000)