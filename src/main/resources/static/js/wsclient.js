document.addEventListener("DOMContentLoaded", function() {

    var buttonSend = document.getElementById("buttonSend");
    var inputTextMessage = document.getElementById("inputMessage");
    var inputPicture = document.getElementById("inputPicture");
    var outputPicture = document.getElementById("outputPicture");
    var outputTextMessage = document.getElementById("outputMessage");

    var buttonConnect = document.getElementById("buttonConnect");
    var inputToken = document.getElementById("inputToken");
    var receiverToken = document.getElementById("receiverToken");

    var socket;
    var token;
    var isConnect;

    buttonConnect.addEventListener("click", onConnect);

    buttonSend.addEventListener("click", sendMessage);
    inputTextMessage.addEventListener("keypress", onEnterInputTextMessage);

    init();

    function init() {
        isConnect = false;

        buttonConnect.innerText = "Connect";
        inputToken.disabled = false;

        buttonSend.disabled = true;
        receiverToken.disabled = true;
        inputTextMessage.disabled = true;
    }

    function holdConnection() {
        isConnect = true;

        buttonConnect.innerText = "Close";
        inputToken.disabled = true;

        buttonSend.disabled = false;
        receiverToken.disabled = false;
        inputTextMessage.disabled = false;
    }

    function onConnect() {
        token = inputToken.value;
        if(!isConnect) {
            if(token) {
                createWsConnect();
            }
        } else {
            socket.close();
        }
    }

    function createWsConnect() {
        socket = new WebSocket("ws://localhost:8080/token/" + token);

        socket.addEventListener('open', wsConnect);
        socket.addEventListener('message', wsGetMessage);
        socket.addEventListener('close', wsClose);
        socket.addEventListener('error', wsError);
    }

    function wsConnect() {
        holdConnection();
    }

    function wsGetMessage(event) {
        var data = event.data;
        console.log("message: " + data);
        if(data) {
            var oldText = outputTextMessage.value;

            var message = JSON.parse(data);
            if (message.text) {
                outputTextMessage.value = message.text + "\n" + oldText;
            }

            if (message.pictureId) {
                outputPicture.src = "http://localhost:8081/getPicture?id=" + message.pictureId;
            }
        }
    }

    function wsClose(event) {
        init();
        if (event.wasClean) {
            console.log('close');
        } else {
            console.log('Alarm close');
        }
        console.log('Code: ' + event.code + ' cause: ' + event.reason);
        init();
    }

    function wsError() {
        console.log("Error: " + error.message);
    }

    function onEnterInputTextMessage(event) {
        if (event.keyCode == 13) {
            sendMessage();
        }
    }

    function sendMessage() {
        var text = inputTextMessage.value;
        var file = inputPicture.files[0];
        var receiverTokenText = receiverToken.value;
        console.log("Send message: " + text + ", token: " + token);

        if (receiverTokenText) {
            if (text) {
                 sendText(receiverTokenText, text);
                 inputTextMessage.value = '';
            }
            if (file) {
                 sendPicture(receiverTokenText, file);
                 inputPicture.value = '';
            }
        }
    }

    function sendText(address, text) {
            socket.send(
                    '{"text":"' + text + '","address":"' + address +'"}'
            );
    }

    function sendPicture(address, file) {
            var reader = new FileReader();
            var rawData = new ArrayBuffer();
            reader.onload = function(e) {
                rawData = e.target.result;
                socket.send('{"picture":"' + bin2base64(rawData) + '", "address":"' + address + '"}');
            }
            reader.readAsArrayBuffer(file);
    }

    function bin2base64(byteArray) {
        return btoa([].reduce.call(
            new Uint8Array(byteArray),
            function (p, c) {
                return p + String.fromCharCode(c);
            },
            ''
        ));
    }
});