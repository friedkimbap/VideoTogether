var player;

var tag = document.createElement('script');
tag.src = "https://www.youtube.com/player_api";
var firstScriptTag = document.getElementsByTagName('script')[0];
firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);

function onYouTubeIframeAPIReady() {
    player = new YT.Player('player', {
        videoId: 'Ox29z5Nf1Uk', // 유튜브 링크 뒤에 id 있음
        playerVars: { // https://developers.google.com/youtube/player_parameters?hl=ko 참조
            'controls': 1, // 플레이어 컨트롤 숨기기
            'fs': 0, // 전체화면 버튼 숨기기
            'modestbranding': 1, // YouTube 로고 숨기기
            'rel': 0, // 관련 동영상 숨기기
            'autoplay': 0, // 자동 재생
            'enablejsapi': 1, // js로 제어 가능토록 하는 거
        },
        events: {
            'onReady': onPlayerReady,
            'onStateChange': onPlayerStateChange
        }
    });
}

function onPlayerReady(event) {
}

function onPlayerStateChange(event) {
    var action;
    if (event.data === YT.PlayerState.PLAYING) {
        action = 'play';
    } else if (event.data === YT.PlayerState.PAUSED) {
        action = 'pause';
    } else if (event.data === YT.PlayerState.ENDED) {
        action = 'stop';
    } else if (event.data === YT.PlayerState.BUFFERING) {
        action = 'pause';
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

function setTime(time) {
    if(player){
        player.seekTo(parseInt(time), true);
    }
}

function setSize(width, height){
    if(player){
        player.width = parseInt(width)+"px";
        player.height = parseInt(height)+"px";
    }
}

function setId(id){
    if(player){
        player.videoId = id;
    }
}

function getTime() {
    if(player){
        return player.getCurrentTime();
    }
}

function getState(){
    if(player) {
        return player.getPlayerState();
    }
}