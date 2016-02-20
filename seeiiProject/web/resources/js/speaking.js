/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function startTest(){
    vistaPrincipal.tagEscuchar.value='';
}
function hablarProbarMicrofono() {
    var elemento = document.getElementById("verificarMicrof");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "Spanish Female");
}

function hablarRespMicrofono() {
    var elemento = document.getElementById("respPregSpeakFMostrada");
    var frase = elemento.value;
    var audio = new Audio('../../resources/sounds/correct.wav');
    audio.play();
    responsiveVoice.speak(frase, "Spanish Female");
}

function pausarAnnyang() {
    annyang.pause();
    annyang.removeCommands();
    annyang.abort();

}

function probarMicrofono() {
    clearInterval(this.progressInterval);
    PF('pb').setValue(0);
    var elemento = document.getElementById("tagEscuchar");
    elemento.value ="";
    var elemento1 = document.getElementById("inTurnFadingTextG");
    "use strict"; // no se puede utilizar variables no declaradas
    if (annyang) {
        //Define las funciones de nuestras órdenes que se ejecutarán.
        var probarMicro = function(palabra) {
            elemento1.style.display = "none";
            this.progressInterval = setInterval(function() {
                if (PF('pb').getValue() < 100) {
                    PF('pb').setValue(PF('pb').getValue() + 50);
                    elemento.value = "Escuchando " + PF('pb').getValue() / 50 + "/2";
                    console.log("INTERVALO: " + PF('pb').getValue());
                    if (PF('pb').getValue() === 100) {
                        PF('dialogVerificarMicrofono').hide();
                        PF('dialogProbarResp').show();
                    }
                }
            }, 1000);
        };
        // Define nuestros comandos.
        var commands = {
            '12345': probarMicro //indicando :palabraEscuchada captura cualquier cosa y llama a la función verificar
        };
        annyang.debug(); // para saber si hay un error en la consola del navegador
        annyang.addCommands(commands);
        annyang.addCallback('errorNetwork', notConnected, this);
        annyang.setLanguage("es-Ec");

        var r = document.getElementById('iconMicro');
        r.style.background = "#7892c2";
        r.style.border = "1px solid #7892c2";
        elemento1.style.display = "block";
        annyang.start(); // Annyang y la Cual solicita permiso al usuario para usar el micrófono

    }

}
function notConnected() {
    var elemento = document.getElementById("tagEscuchar");
    var elemento1 = document.getElementById("inTurnFadingTextG");
    var r = document.getElementById('iconMicro');
    r.style.background = "#BA98BA";
    r.style.border = "1px solid #BA98BA;";
    elemento1.style.display = "none";
    elemento.value = "Error de conexión";
    pausarAnnyang();
}

function escucharAnnyangF(element, elementMos, elementoPos) {

    "use strict"; // no se puede utilizar variables no declaradas
    if (annyang) {
        //Define las funciones de nuestras órdenes que se ejecutarán.

        var elemento = document.getElementById("itemSelectedF");
        var respuesta = elemento.value;
        var verificarF = function(palabra) {
            console.log("respuesta esperada: " + respuesta);
            console.log("respuesta escuchada: " + palabra);

        };
        // Define nuestros comandos.
        var commands = {
            ':palabraEscuchada': verificarF //indicando :palabraEscuchada captura cualquier cosa y llama a la función verificar
        };
        annyang.debug(); // para saber si hay un error en la consola del navegador
        annyang.maxAlternatives = 10;
        annyang.addCommands(commands);
        annyang.addCallback('resultMatch', function(said, verificarF, phrases) {
            console.log(said, verificarF);
            console.log('An array of possible alternative phrases the user might\'ve said');
            console.log(phrases);
            var correctoPhrases = false;
            for (var i = 0; i < phrases.length; i++) {
                if (phrases[i] === respuesta) {
                    correctoPhrases = true;
                }
            }
            if (correctoPhrases === true) {
                element.value = "Correct. " + respuesta;
                elementMos.value = respuesta;
                document.getElementById(elementoPos).value = "true";
                PF('dialogResultadoSpeakF').hide();
                PF('dialogRespSpeakF').show();

            } else {
                element.value = "Incorrect. The correct answer was " + respuesta;
                elementMos.value = respuesta;
                document.getElementById(elementoPos).value = "false";
                PF('dialogResultadoSpeakF').hide();
                PF('dialogRespSpeakF').show();
            }

        });
        var r = document.getElementById('iconListenF');
        r.style.background = "#5cbf2a";
        r.style.border = "1px solid #18ab29";
        annyang.start(); // Annyang y la Cual solicita permiso al usuario para usar el micrófono

    }
}

function escucharAnnyangMD(element, elementMos, elementPos) {

    "use strict"; // no se puede utilizar variables no declaradas
    if (annyang) {
        //Define las funciones de nuestras órdenes que se ejecutarán.

        var elemento = document.getElementById("itemSelectedMD");
        var respuesta = elemento.value;
        var verificar = function(palabra) {
            console.log("respuesta esperada: " + respuesta);
            console.log("respuesta escuchada: " + palabra);

        };
        // Define nuestros comandos.
        var commands = {
            ':palabraEscuchada': verificar //indicando :palabraEscuchada captura cualquier cosa y llama a la función verificar
        };
        annyang.debug(); // para saber si hay un error en la consola del navegador
        annyang.maxAlternatives = 10;
        annyang.addCommands(commands);
        annyang.addCallback('resultMatch', function(said, verificar, phrases) {
            console.log(said, verificar);
            console.log('An array of possible alternative phrases the user might\'ve said');
            console.log(phrases);
            var correctoPhrases = false;
            for (var i = 0; i < phrases.length; i++) {
                if (phrases[i] === respuesta) {
                    correctoPhrases = true;
                }
            }
            if (correctoPhrases === true) {
                element.value = "Correct. " + respuesta;
                elementMos.value = respuesta;
                document.getElementById(elementPos).value = "true";
                PF('dialogResultadoSpeakMD').hide();
                PF('dialogRespSpeakMD').show();

            } else {
                element.value = "Incorrect. The correct answer was " + respuesta;
                elementMos.value = respuesta;
                document.getElementById(elementPos).value = "false";
                PF('dialogResultadoSpeakMD').hide();
                PF('dialogRespSpeakMD').show();
            }

        });
        var r = document.getElementById('iconListenMD');
        r.style.background = "#5cbf2a";
        r.style.border = "1px solid #18ab29";
        annyang.start(); // Annyang y la Cual solicita permiso al usuario para usar el micrófono

    }
}
function retornarValor(element) {
    return document.getElementById(element).value;
}
function obtenerBoolean(elemento) {
    return elemento.value;
}

function hablarListen() {
    var elemento = document.getElementById("resultadoPregListen");
    var elemento1 = document.getElementById("soundListen");
    var frase = elemento.value;
    var audio;
    if (elemento1.value === "true") {
        audio = new Audio('../../resources/sounds/correct.wav');
        audio.play();
    } else {
        audio = new Audio('../../resources/sounds/incorrect.wav');
        audio.play();
    }
    audio.addEventListener('ended', function() {
        responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
    }, false);
}
function hablarSpeakF() {
    var elemento = document.getElementById("resultadoPregSpeakF");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
}

function hablarSpeakMD() {
    var elemento = document.getElementById("resultadoPregSpeakMD");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
}

function hablarRespSpeakF() {
    var elemento = document.getElementById("respPregSpeakF");
    var elemento1 = document.getElementById("frmRespSpeakF:correctSpeakF");
    var frase = elemento.value;
    var audio;
    if (elemento1.value === "true") {
        audio = new Audio('../../resources/sounds/correct.wav');
        audio.play();
    } else {
        audio = new Audio('../../resources/sounds/incorrect.wav');
        audio.play();
    }
    audio.addEventListener('ended', function() {
        responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
    }, false);
}

function hablarRespSpeakMD() {
    var elemento = document.getElementById("respPregSpeakMD");
    var elemento1 = document.getElementById("frmRespSpeakMD:correctoSpeakMD");
    var frase = elemento.value;
    var audio;
    if (elemento1.value === "true") {
        audio = new Audio('../../resources/sounds/correct.wav');
        audio.play();
    } else {
        audio = new Audio('../../resources/sounds/incorrect.wav');
        audio.play();
    }
    audio.addEventListener('ended', function() {
        responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
    }, false);
}

function hablarSalir() {
    var elemento = document.getElementById("enunciadoSalir");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
}

function speakTitle() {
    var elemento = document.getElementById("tituloPag");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female", {rate: 0.85});
}

function leerPregListen() {
    var enunciado = document.getElementById("enunciadoPregListen1").value;
    responsiveVoice.speak(enunciado, "US English Female", {rate: 0.85});
}

function leerPregSpeakF() {
    var enunciado = document.getElementById("enunciadoPregSpeakF1").value;
    responsiveVoice.speak(enunciado, "US English Female", {rate: 0.85});
}

function leerPregSpeakMD() {
    var enunciado = document.getElementById("enunciadoPregSpeakMD1").value;
    responsiveVoice.speak(enunciado, "US English Female", {rate: 0.85});
}

function laugh() {
    var audio = new Audio('../../resources/sounds/laugh.mp3');
    audio.play();
}