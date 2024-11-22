var player;
var socket = new WebSocket('ws://localhost:8887');

function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
        videoId: 'TfatKL3cI7I', // 유튜브 링크 뒤에 id 있음
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
    if (event.data == YT.PlayerState.PLAYING) {
        action = 'play';
    } else if (event.data == YT.PlayerState.PAUSED) {
        action = 'pause';
    } else if (event.data == YT.PlayerState.ENDED) {
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

function stopVideo() {
    if (player) {
        player.stopVideo();
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
