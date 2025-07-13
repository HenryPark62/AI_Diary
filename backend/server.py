import os
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
from dotenv import load_dotenv

# .env 파일에서 환경 변수 불러오기
load_dotenv()

OPENAI_API_KEY = os.getenv('OPENAI_API_KEY')
print('OPENAI_API_KEY:', OPENAI_API_KEY)

app = Flask(__name__)
CORS(app)

@app.route('/api/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        messages = data.get('messages', [])
        headers = {
            'Authorization': f'Bearer {OPENAI_API_KEY}',
            'Content-Type': 'application/json'
        }
        payload = {
            'model': 'gpt-3.5-turbo',
            'messages': messages
        }
        response = requests.post(
            'https://api.openai.com/v1/chat/completions',
            headers=headers,
            json=payload
        )
        response.raise_for_status()
        return jsonify(response.json())
    except Exception as e:
        return jsonify({'error': 'OpenAI API 호출 실패'}), 500

if __name__ == '__main__':
    app.run(port=5000)
