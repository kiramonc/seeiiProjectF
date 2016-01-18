/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases.RedNeuronal;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

/**
 *
 * @author silvy
 */
public class RedNeuronal {

    private String resultado;
    private String imgResultado;

    public void redNeuronal(int puntaje, int tiempo, int error) throws Exception {
        //si puntaje >= 200 entonces aprendido
        //si tiempo <= 240 (4 minutos) entonces aprendido
        //si errores <= 3 entonces aprendido
        String[] dato = {obtnerPuntaje(puntaje), obtenerTiempo(tiempo), obtenerErrores(error)};

        ConverterUtils.DataSource con = new ConverterUtils.DataSource("C:\\Users\\USUARIO\\Documents\\SILVIIS\\10 Modulo\\2.ANTEPROYECTOS DE TESIS\\Proyecto\\Aplicacion\\redeAprendizaje.arff");
//        ConverterUtils.DataSource con = new ConverterUtils.DataSource("E:\\Unl\\10 Modulo\\2.ANTEPROYECTOS DE TESIS\\Proyecto\\Aplicacion\\redeAprendizaje.arff");

        Instances instances = con.getDataSet();
        System.out.println(instances);
        instances.setClassIndex(instances.numAttributes() - 1);

        MultilayerPerceptron mp = new MultilayerPerceptron();
        mp.buildClassifier(instances);

        Evaluation evalucion = new Evaluation(instances);
        evalucion.evaluateModel(mp, instances);
        System.out.println(evalucion.toSummaryString());
        System.out.println(evalucion.toMatrixString());

        String datosEntrada = null;
        String datosSalida = "no se puede predecir";
        for (int i = 0; i < instances.numInstances(); i++) {
            double predecido = mp.classifyInstance(instances.instance(i));
            datosEntrada = dato[0] + " " + dato[1] + " " + dato[2];
            if ((int) instances.instance(i).value(0) == Integer.parseInt(dato[0])
                    && (int) instances.instance(i).value(1) == Integer.parseInt(dato[1])
                    && (int) instances.instance(i).value(2) == Integer.parseInt(dato[2])) {
                datosSalida = instances.classAttribute().value((int) predecido);
            }
        }
        System.out.println("DATOS DE ENTRADA: " + datosEntrada);
        System.out.println("SALIDA PREDECIDA: " + datosSalida);

        switch (datosSalida) {
            case "0":
                resultado = "Excelente ha aprendido";
                imgResultado = "Excelente.jpg";
                System.out.println("Excelente ha aprendido");
                break;
            case "1":
                resultado = "Disminuir Errores";
                imgResultado = "Bueno.jpg";
                System.out.println("Disminuir Errores");
                break;
            case "2":
                resultado = "Disminuir Tiempo";
                imgResultado = "Bueno.jpg";
                System.out.println("Disminuir Tiempo");
                break;
            case "3":
                resultado = "Disminuir Errores y tiempo";
                imgResultado = "pensando.jpg";
                System.out.println("Disminuir Errores y tiempo");
                break;
            case "4":
                resultado = "Subir Puntaje";
                if (puntaje == 0) {
                    imgResultado = "mal.jpg";
                } else {
                    if (puntaje > 350) {
                        imgResultado = "pensando.jpg";
                        if (puntaje >= 700) {
                            imgResultado = "Bueno.jpg";
                        }
                    } else {
                        imgResultado = "triste.jpg";
                    }
                }
                System.out.println("Subir Puntaje");
                break;
            case "5":
                resultado = "Subir Puntaje y disminuir Errores";
                if (puntaje == 0) {
                    imgResultado = "mal.jpg";
                } else {
                    if (puntaje > 350) {
                       imgResultado = "pensando.jpg";
                        if (puntaje >= 700) {
                            imgResultado = "Bueno.jpg";
                        }
                    } else {
                        imgResultado = "triste.jpg";
                    }
                }
                System.out.println("Subir Puntaje y disminuir Errores");
                break;
            case "6":
                resultado = "Subir Puntaje y disminuir Tiempo";
                if (puntaje == 0) {
                    imgResultado = "mal.jpg";
                } else {
                    if (puntaje > 350) {
                        imgResultado = "pensando.jpg";
                        if (puntaje >= 700) {
                            imgResultado = "Bueno.jpg";
                        }
                    } else {
                        imgResultado = "triste.jpg";
                    }
                }
                System.out.println("Subir Puntaje y disminuir Tiempo");
                break;
            case "7":
                resultado = "Ponle mas Empeño";
                imgResultado = "mal.jpg";
                System.out.println("Ponle mas Empeño");
                break;
            default:
                resultado = "Verifique entradas, no se puede predecir";
                imgResultado = "Error.jpg";
                System.out.println("Verifique entradas, no se puede predecir");
                break;
        }
    }

    public String obtnerPuntaje(int punto) {
        int valorPuntos;
        if (punto >= 950) {
            valorPuntos = 1;
        } else {
            valorPuntos = 0;
        }
        String puntaje = "" + valorPuntos;
        return puntaje;
    }

    public String obtenerTiempo(int tiempo) {
        int valorTiempo;
        if (tiempo >= 240) { //mayor a 120(4 minutos)
            valorTiempo = 0;
        } else {
            valorTiempo = 1;
        }
        String tiemposEntrenar = "" + valorTiempo;
        return tiemposEntrenar;
    }

    public String obtenerErrores(int errors) {
        int valorError;
        if (errors > 2) {
            valorError = 0;
        } else {
            valorError = 1;
        }
        String erroresEntrenar = "" + valorError;
        return erroresEntrenar;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getImgResultado() {
        return imgResultado;
    }

    public void setImgResultado(String imgResultado) {
        this.imgResultado = imgResultado;
    }

}
