var player;
var socket = new WebSocket('ws://localhost:54321');

var tag = document.createElement('script');
tag.src = "https://www.youtube.com/player_api";
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
        videoId: 'TfatKL3cI7I', // 유튜브 링크 뒤에 id 있음
        playerVars: { // https://developers.google.com/youtube/player_parameters?hl=ko 참조
            'controls': 0, // 플레이어 컨트롤 숨기기
            'fs': 0, // 전체화면 버튼 숨기기
            'modestbranding': 1, // YouTube 로고 숨기기
            'rel': 0, // 관련 동영상 숨기기
            'autoplay': 1, // 자동 재생
            'enablejsapi': 1,
        },
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}

function onPlayerReady(event) {
    console.log('Player ready');
}

function onPlayerStateChange(event) {
    var action;
    if (event.data === YT.PlayerState.PLAYING) {
        action = 'play';
    } else if (event.data === YT.PlayerState.PAUSED) {
        action = 'pause';
    } else if (event.data === YT.PlayerState.ENDED) {
        action = 'stop';
    }
    if (action) {
        sendMessage(action);
    }
}

function playVideo() {
    if (player) {
        player.playVideo();
    }
}

function pauseVideo() {
    if (player) {
        player.pauseVideo();
    }
}

function sendMessage(action) {
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(action);
    }
}

socket.onmessage = function(event) {
    var message = event.data;
    if (message === 'play') {
        playVideo();
    } else if (message === 'pause') {
        pauseVideo();
    } else if (message === 'stop') {
        stopVideo();
    }
};
