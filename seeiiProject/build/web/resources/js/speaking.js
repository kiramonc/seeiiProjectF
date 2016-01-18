/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function pausarAnnyang() {
    annyang.pause();
    annyang.removeCommands();
    annyang.abort();

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
        annyang.maxAlternatives= 10;
        annyang.addCommands(commands);
        annyang.addCallback('resultMatch', function(said, verificarF, phrases) {
            console.log(said, verificarF);
            console.log('An array of possible alternative phrases the user might\'ve said');
            console.log(phrases);
            var correctoPhrases= false;
            for(var i=0; i<phrases.length; i++){
            if (phrases[i] === respuesta) {
                correctoPhrases= true;
            }}
            if (correctoPhrases=== true) {
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
        var r= document.getElementById('iconListenF')
        r.style.background="#5cbf2a";
        r.style.border="1px solid #18ab29";
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
            var correctoPhrases= false;
            for(var i=0; i<phrases.length; i++){
            if (phrases[i] === respuesta) {
                correctoPhrases= true;
            }}
            if (correctoPhrases=== true) {
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
        var r= document.getElementById('iconListenMD')
        r.style.background="#5cbf2a";
        r.style.border="1px solid #18ab29";
        annyang.start(); // Annyang y la Cual solicita permiso al usuario para usar el micrófono
    
    }
}
function retornarValor(element){
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
    if(elemento1.value==="true"){
        audio= new Audio('../../resources/sounds/correct.wav');
        audio.play();
    }else{
        audio= new Audio('../../resources/sounds/incorrect.wav');
        audio.play();
    }
    audio.addEventListener('ended', function () {
    responsiveVoice.speak(frase, "US English Female");
    }, false);
}
function hablarSpeakF() {
    var elemento = document.getElementById("resultadoPregSpeakF");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female");
}
function hablarSpeakMD() {
    var elemento = document.getElementById("resultadoPregSpeakMD");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female");
}
function hablarRespSpeakF() {
    var elemento = document.getElementById("respPregSpeakF");
    var elemento1 = document.getElementById("frmRespSpeakF:correctSpeakF");
    var frase = elemento.value;
    var audio;
    if(elemento1.value==="true"){
        audio= new Audio('../../resources/sounds/correct.wav');
        audio.play();
    }else{
        audio= new Audio('../../resources/sounds/incorrect.wav');
        audio.play();
    }
    audio.addEventListener('ended', function () {
    responsiveVoice.speak(frase, "US English Female");
    }, false);
}
function hablarRespSpeakMD() {
    var elemento = document.getElementById("respPregSpeakMD");
    var elemento1 = document.getElementById("frmRespSpeakMD:correctoSpeakMD");
    var frase = elemento.value;
    var audio;
    if(elemento1.value==="true"){
        audio= new Audio('../../resources/sounds/correct.wav');
        audio.play();
    }else{
        audio= new Audio('../../resources/sounds/incorrect.wav');
        audio.play();
    }
    audio.addEventListener('ended', function () {
    responsiveVoice.speak(frase, "US English Female");
    }, false);
}
function hablarSalir() {
    var elemento = document.getElementById("enunciadoSalir");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female");
}
function speakTitle() {
    var elemento = document.getElementById("tituloPag");
    var frase = elemento.value;
    responsiveVoice.speak(frase, "US English Female");
}
function leerPregListen() {
    var enunciado = document.getElementById("enunciadoPregListen1").value;
    responsiveVoice.speak(enunciado, "US English Female");
}

function leerPregSpeakF(){
    var enunciado = document.getElementById("enunciadoPregSpeakF1").value;
    responsiveVoice.speak(enunciado, "US English Female");
}
function leerPregSpeakMD(){
    var enunciado = document.getElementById("enunciadoPregSpeakMD1").value;
    responsiveVoice.speak(enunciado, "US English Female");
}

function laugh(){
    var audio= new Audio('../../resources/sounds/laugh.mp3');
    audio.play();
}