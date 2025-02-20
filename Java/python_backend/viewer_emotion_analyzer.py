import cv2
import numpy as np
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import img_to_array
from collections import defaultdict
import time
import logging
from pathlib import Path
import json
import os

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants
VIDEO_PATH = "hello.mp4"  # Replace with your video file path
EMOTION_LABELS = ['Surprise', 'Fear', 'Disgust', 'Happy', 'Sad', 'Angry', 'Neutral']

# Genre-emotion mappings
GENRE_EMOTION_MAPPINGS = {
    'comedy': {
        'expected': ['Happy', 'Surprise'],
        'weights': {'Happy': 0.7, 'Surprise': 0.3}
    },
    'horror': {
        'expected': ['Fear', 'Surprise', 'Disgust'],
        'weights': {'Fear': 0.5, 'Surprise': 0.3, 'Disgust': 0.2}
    },
    'drama': {
        'expected': ['Sad', 'Neutral', 'Happy'],
        'weights': {'Sad': 0.4, 'Neutral': 0.3, 'Happy': 0.3}
    },
    'action': {
        'expected': ['Surprise', 'Fear', 'Happy'],
        'weights': {'Surprise': 0.4, 'Fear': 0.3, 'Happy': 0.3}
    },
    'romance': {
        'expected': ['Happy', 'Sad', 'Surprise'],
        'weights': {'Happy': 0.5, 'Sad': 0.3, 'Surprise': 0.2}
    },
    'thriller': {
        'expected': ['Fear', 'Surprise', 'Neutral'],
        'weights': {'Fear': 0.4, 'Surprise': 0.4, 'Neutral': 0.2}
    }
}

class ViewerEmotionAnalyzer:
    def __init__(self):
        self.emotion_counts = defaultdict(int)
        self.total_frames = 0
        self.processed_frames = 0
        self.start_time = None
        self.emotion_timeline = []
        
        # Load model and cascade classifier
        try:
            model_path = r"C:\Uni\miniProject\Autobot\Java\python_backend\best_CNNModel.keras"
            if os.path.exists(model_path):
                print("✅ Model file found!")
            else:
                print("❌ Model file NOT found. Check the file path.")

            # self.model = load_model(r"C:\Uni\miniProject\Autobot\Java\python_backend\best_CNNModel.keras")
            self.model = load_model(model_path)
            self.face_cascade = cv2.CascadeClassifier(cv2.data.haarcascades + 'haarcascade_frontalface_default.xml')
        except Exception as e:
            logger.error(f"Failed to load model or cascade classifier: {str(e)}")
            raise

    def analyze_viewer_emotion(self, frame):
        """Analyze viewer's emotion in a single frame"""
        self.total_frames += 1
        
        if self.start_time is None:
            self.start_time = time.time()

        try:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = self.face_cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(30, 30))
            
            for (x, y, w, h) in faces:
                try:
                    face_roi = frame[y:y+h, x:x+w]
                    face_roi = cv2.resize(face_roi, (100, 100))
                    face_roi = img_to_array(face_roi)
                    face_roi = face_roi.astype('float32') / 255.0
                    face_roi = np.expand_dims(face_roi, axis=0)
                    
                    prediction = self.model.predict(face_roi, verbose=0)
                    emotion_idx = np.argmax(prediction[0])
                    emotion_label = EMOTION_LABELS[emotion_idx]
                    confidence = prediction[0][emotion_idx]
                    
                    if confidence > 0.4:
                        self.emotion_counts[emotion_label] += 1
                        self.emotion_timeline.append({
                            'emotion': emotion_label,
                            'confidence': float(confidence)
                        })
                    
                    cv2.rectangle(frame, (x, y), (x+w, y+h), (0, 255, 0), 2)
                    label = f'{emotion_label}: {confidence*100:.2f}%'
                    cv2.putText(frame, label, (x, y-10),
                              cv2.FONT_HERSHEY_SIMPLEX, 0.9,
                              (0, 255, 0), 2)
                    
                except Exception as e:
                    logger.warning(f"Error processing face: {str(e)}")
                    continue
            
            self.processed_frames += 1
            return frame, emotion_label if 'emotion_label' in locals() else None
            
        except Exception as e:
            logger.error(f"Error processing frame: {str(e)}")
            return frame, None

    def generate_report(self, genre):
        """Generate analysis report"""
        if genre not in GENRE_EMOTION_MAPPINGS:
            return {"error": "Invalid genre specified"}
            
        total_emotions = sum(self.emotion_counts.values())
        if total_emotions == 0:
            return {"error": "No emotions detected"}
            
        duration = time.time() - self.start_time
        
        emotion_percentages = {
            emotion: (count / total_emotions * 100)
            for emotion, count in self.emotion_counts.items()
        }
        
        expected_emotions = GENRE_EMOTION_MAPPINGS[genre]['expected']
        weights = GENRE_EMOTION_MAPPINGS[genre]['weights']
        
        match_score = sum(
            weights.get(emotion, 0) * emotion_percentages.get(emotion, 0)
            for emotion in expected_emotions
        ) / 100
        
        return {
            "genre": genre,
            "duration": round(duration, 2),
            "total_frames": self.total_frames,
            "processed_frames": self.processed_frames,
            "total_emotions_detected": total_emotions,
            "emotion_distribution": {k: round(v, 2) for k, v in emotion_percentages.items()},
            "genre_match_score": round(match_score, 3),
            "primary_emotion": max(self.emotion_counts.items(), key=lambda x: x[1])[0] if total_emotions > 0 else None,
            "expected_emotions": expected_emotions,
            "sync_analysis": {
                "score": round(match_score, 3),
                "interpretation": "Good sync" if match_score > 0.6 else "Moderate sync" if match_score > 0.3 else "Poor sync",
                "details": f"Viewer reactions matched {match_score*100:.1f}% with expected {genre} reactions"
            },
            "emotion_timeline": self.emotion_timeline
        }

def analyze_viewer_reaction(genre):
    """Main function to analyze viewer reactions"""
    if not Path(VIDEO_PATH).exists():
        print(f"Error: Video file not found at {VIDEO_PATH}")
        return
        
    if genre not in GENRE_EMOTION_MAPPINGS:
        print(f"Invalid genre. Supported genres: {', '.join(GENRE_EMOTION_MAPPINGS.keys())}")
        return
    
    try:
        analyzer = ViewerEmotionAnalyzer()
        
        video = cv2.VideoCapture(VIDEO_PATH)
        if not video.isOpened():
            print("Error: Could not open video file")
            return
            
        webcam = cv2.VideoCapture(0)
        if not webcam.isOpened():
            print("Error: Could not open webcam")
            return
        
        print(f"Playing video and analyzing viewer reactions. Press 'q' to stop.")
        
        while True:
            video_ret, video_frame = video.read()
            if not video_ret:
                break
                
            webcam_ret, webcam_frame = webcam.read()
            if not webcam_ret:
                break
            
            timestamp = video.get(cv2.CAP_PROP_POS_MSEC) / 1000.0
            processed_frame, emotion = analyzer.analyze_viewer_emotion(webcam_frame, timestamp)
            
            cv2.imshow('Video Playback', video_frame)
            cv2.imshow('Viewer Analysis', processed_frame)
            
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
        
        video.release()
        webcam.release()
        cv2.destroyAllWindows()
        
        report = analyzer.generate_report(genre)
        
        report_path = f"viewer_analysis_report_{int(time.time())}.json"
        with open(report_path, 'w') as f:
            json.dump(report, f, indent=4)
            
        print(f"\nAnalysis complete! Report saved to {report_path}")
        return report
        
    except Exception as e:
        print(f"Error during analysis: {str(e)}")
        return None

if __name__ == "__main__":
    # Simply specify the genre and run
    genre = 'neutral'  # Change this to your desired genre
    report = analyze_viewer_reaction(genre)
    
    if report and 'error' not in report:
        print("\nAnalysis Summary:")
        print(f"Genre Match Score: {report['genre_match_score']}")
        print(f"Sync Analysis: {report['sync_analysis']['interpretation']}")
        print(f"Primary Emotion: {report['primary_emotion']}")
        print("\nEmotion Distribution:")
        for emotion, percentage in report['emotion_distribution'].items():
            print(f"{emotion}: {percentage}%")