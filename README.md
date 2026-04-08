# Cammate

📸 Cammate

사진 찍히는 사람이 실시간으로 카메라 화면을 확인할 수 있는 Android 화면 공유 앱


📌 프로젝트 소개
사진을 찍을 때, 찍히는 사람은 자신이 카메라에 어떻게 나오고 있는지 확인할 수 없습니다.
Cammate는 촬영자의 화면을 실시간으로 공유하여, 피사체가 원하는 구도와 각도로 찍히고 있는지 직접 확인할 수 있도록 합니다.
블루투스를 통해 주변 사용자를 탐색하기 때문에, 전화번호 공유 없이도 빠르게 연결할 수 있습니다.

💡 기획 배경
기존 카메라 앱에서는 촬영되는 모습을 실시간으로 확인하기 어렵습니다.
여러 번 촬영하고 결과물을 확인하는 과정에서 시간이 낭비되고, 특히 줄을 서서 기다리는 상황에서는 더욱 불편합니다.
Cammate는 이 문제를 실시간 화면 공유로 해결합니다.

🎯 주요 기능

📡 WebRTC 기반 실시간 화면 공유 — 촬영자의 카메라 화면을 피사체에게 실시간 전송
🔵 Bluetooth 주변 탐색 — 전화번호 없이 주변 사용자와 빠르게 연결
💬 WebSocket 채팅 — 이모티콘·리액션으로 간편하게 피드백 전달


🛠 기술 스택
구분기술FrontendAndroid (Kotlin)BackendSpring Boot, MySQL실시간 통신WebRTC, WebSocket네트워크STUN Server, TURN Server

🏗 아키텍처
Client 1 (촬영자)
  1. 방 생성 → DB 저장 및 WebSocket Room 생성
  2. 'enter-room' 이벤트 수신 → 수락 여부 창 표시
  3. 수락 시 'accept' 이벤트 전송

Client 2 (피사체)
  2. Bluetooth로 주변 방 탐색
  3. 방 리스트에서 입장할 방 클릭
  4. 'enter-room' 이벤트 전송
  5. 'accept' 이벤트 수신 후 ICE Candidate 과정 시작
