/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases.RedBayesiana;

import Pojo.Concepto;
import Pojo.Pregunta;
import Pojo.Tema;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.openmarkov.core.exception.ConstraintViolationException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.InvalidStateException;
import org.openmarkov.core.exception.NoFindingException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNode;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Util;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeManager;
import org.openmarkov.inference.variableElimination.VariableElimination;
import org.openmarkov.io.probmodel.PGMXReader;
import org.openmarkov.io.probmodel.PGMXWriter;

/**
 *
 * @author KathyR
 */
public class CrearBayesDynamic {

    final private double valorSi = 0.15;
    private final State[] estados = new State[2];
    private String ruta;

    public CrearBayesDynamic() {
        /**
         * Se define los estados
         */
        estados[0] = new State("sí");
        estados[1] = new State("no");
        ServletContext servletContex= (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        this.ruta= (String) servletContex.getRealPath("/resources/RedesBayesianas/BNDiagnostic");
//        this.ruta = "C:\\Users\\KathyR\\Dropbox\\Décimo\\Trabajo de titulación\\Red bayesiana";
    }

    /**
     *
     * @return valores de los estados donde la nueva variable es la condicionada
     * probabilidades a priori de los conceptos en tiempo 0
     */
    private double[] setValoresPriori() {
        double[] valores = new double[2]; // dos estados sí y no
        valores[0] = valorSi; // valor 1 
        valores[1] = 1 - valores[0]; // valor 0 
        return valores;
    }

    /**
     *
     * @param valorPositivo = valor positivo calculado de la inferencia
     * @return valores a priori nuevos para el concepto
     */
    private double[] setValoresConceptos(double valorPositivo) {
        double[] valores = new double[2]; // dos estados sí y no
        valores[0] = valorPositivo; // valor 1 
        valores[1] = 1 - valores[0]; // valor 0 
        return valores;
    }

    /**
     *
     * @param listaVariables = variables que forman parte del TablePotential
     * @param posiblesRespuestas = int del número de items que contiene la
     * pregunta
     * @param dificultad = double del nivel de dificultad de la pregunta
     * @return valores de los estados donde la nueva variable es la condicionante
     */
    private double[] setValoresCondicionante(List<Variable> listaVariables, int posiblesRespuestas, double dificultad, double descuido, double iDiscrimina, int numConceptos) {
        //Asignación de valores
        double s = descuido; // Factor descuido 0.02
        double c; // Factor adivinanza
        if (posiblesRespuestas <= 1) {
            c = 0;
        } else {
            c = 1.0 / posiblesRespuestas;
        }

        double a = iDiscrimina; // Índice de discriminación 0.4
        double b = dificultad;

        int contador = 0;
        int longitud = listaVariables.size();
        double[] valores = new double[(int) Math.pow(2, longitud)];
        String numBin;
        for (int i = 0; i < (int) Math.pow(2, longitud); i++) {
            if (i % 2 == 0) { // valores para si
                numBin = Integer.toBinaryString(i);
                StringBuilder builder = new StringBuilder(numBin);
                String numBinInvertida = builder.reverse().toString();
                while (numBinInvertida.length() < longitud) {
                    numBinInvertida = numBinInvertida + 0;
                }
                for (int x = 0; x < numBin.length(); x++) {
                    if ((numBin.charAt(x) == '1')) {
                        contador++; // representan los no
                    }
                }
                contador = longitud - 1 - contador; // número de sí
                if ((listaVariables.size() - 1) != 1) { // cuando se involucra más de un concepto
                    if (contador == (longitud - 1)) { // todos si
                        valores[i] = 1 - s;
                    } else {
                        double x = contador;
                        if (x == 0 && (dificultad == 0.9||dificultad==0.89)) { // Caso de pregunta fácil y si no se conoce ningún concepto
                            valores[i] = 0.0033333333;
                        } else {
                            valores[i] = 1 - ((1 - c) * (1 + Math.exp(-1.7 * a * b)) / (1 + Math.exp(1.7 * a * (x - b)))); // Valor 1
                        }
                    }
                } else { // solo hay un concepto
                    if (contador == (longitud - 1)) { // se conoce el concepto
                        valores[i] = 1 - s;
                    } else { // no se conoce el concepto
                        if (c != 0) // más de un ítem
                        {
                            valores[i] = 0.12; // factor adivinanza
                        } else {// adivinanza mínima
                            if(dificultad==3){
                                valores[i] = 0.12; // pregunta speak m 
                            }else{
                                valores[i] = 0.13; // pregunta SpeakD
                            }
                        }
                    }
                }

                contador = 0;
            } else {
                valores[i] = 1 - valores[i - 1]; // Valor 0
            }
        }
        return valores;
    }

    public int getNumPreguntas(String nombreTema) {
        //Type CHANCE
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
            List<Variable> listPreguntas = probNet1.getVariables(NodeType.CHANCE);
            inputStream.close();
            return listPreguntas.size();
        } catch (FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } 
        return -1;
    }

    private int getNumConceptos(String nombreTema) {
        //Type DECISION
        InputStream inputStream;
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
            List<Variable> listaConceptos = probNet1.getVariables(NodeType.DECISION);
            inputStream.close();
            return listaConceptos.size();
        } catch (FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }

    private List<Variable> getVariablesConceptos(String nombreTema) {
        //Type DECISION
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            List<Variable> listaConceptos;
            try (InputStream inputStream = new FileInputStream(archivoPgmx)) {
                PGMXReader pgmxReader = new PGMXReader();
                probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
                listaConceptos = probNet1.getVariables(NodeType.DECISION);
                inputStream.close();
            }
            return listaConceptos;
        } catch (FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * 
     * @param nombreTema nombre del archivo que contendrá los conceptos y preguntas 
     */
    public void crearRedTema(String nombreTema){
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            ProbNet probNet1 = new ProbNet(org.openmarkov.core.model.network.type.BayesianNetworkType.getUniqueInstance());
            probNet1.setNetworkType(new NetworkTypeManager().getNetworkType("BayesianNetwork"));

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (WriterException | ConstraintViolationException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param nombreTema String tema al que pertenece
     * @param nombreConcepto String nombre del Concepto a crear
     */
    public void crearConcepto(String nombreTema, String nombreConcepto){
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();

            Variable concepto = new Variable(nombreConcepto); // Nueva variable pregunta
            concepto.setVariableType(VariableType.FINITE_STATES);
            concepto.setStates(estados);

            TablePotential tablaPConcepto = concepto.createDeltaTablePotential(0); // Variable hijo
            tablaPConcepto.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesConcepto = new ArrayList<>();
            variablesConcepto.add(concepto); // Variable Condicionada
            tablaPConcepto.setVariables(variablesConcepto); // Establecer Variables
            tablaPConcepto.setValues(setValoresPriori()); // Establecer valores

            /**
             * Crear nodos
             */
            ProbNode nodo1 = new ProbNode(probNet1, concepto, NodeType.DECISION);
            nodo1.setPotential(tablaPConcepto);
            probNet1.addProbNode(nodo1);

            /**
             * Escritura de la red
             */
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            inputStream.close();

        } catch (WriterException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidStateException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param nombreTema Nombre del Tema a crear
     * @param preguntaT
     * @param nombreConceptos Conceptos relacionados con la pregunta
     * @param numItems número de items de la pregunta
     */
    public void crearPregunta(String nombreTema, Pregunta preguntaT, List<Concepto> nombreConceptos, int numItems){
        int numConceptos = getNumConceptos(nombreTema);
        try {
            String file = ruta + "\\" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();

            String nombrePregunta = preguntaT.getNombrePreg();

            /**
             * CREACIÓN DE VARIABLES
             */
            Variable pregunta = new Variable(nombrePregunta); // Variable pregunta (Concepto+id)
            pregunta.setVariableType(VariableType.FINITE_STATES);
            pregunta.setStates(estados);

            //Potencial Pregunta
            TablePotential tablaPPregunta = pregunta.createDeltaTablePotential(0); // Variable hijo
            tablaPPregunta.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaV = new ArrayList<>();
            listaV.add(pregunta); // Variable Condicionada
            Variable concepto; // Variable Unidad (Hijo)
            for (Concepto nombreConcepto : nombreConceptos) {
                concepto = probNet1.getVariable(nombreConcepto.getNombreConcepto()); // Variable Unidad (Hijo)
                listaV.add(concepto);
            }
            tablaPPregunta.setVariables(listaV); // Establecer Variables
            tablaPPregunta.setValues(setValoresCondicionante(listaV, numItems, preguntaT.getTipopregunta().getDificultad(), preguntaT.getTipopregunta().getFdescuido(), preguntaT.getTipopregunta().getIndiceDis(), numConceptos)); // Establecer valores

            /**
             * Crear nodos
             */
            ProbNode nodo1 = new ProbNode(probNet1, pregunta, NodeType.CHANCE);
            nodo1.setPotential(tablaPPregunta);
            probNet1.addProbNode(nodo1);

            /**
             * Escritura de la red
             */
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            inputStream.close();
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidStateException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param nombreTema Tema al que pertenece
     * @param nombreConcepto nombre del concepto que se va a editar
     * @param nuevoNombre nuevo nombre para el concepto
     */
    public void editarConcepto(String nombreTema, String nombreConcepto, String nuevoNombre){
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();

            Variable concepto = probNet1.getVariable(nombreConcepto);
            concepto.setName(nuevoNombre);

            /**
             * Potencial Concepto
             */
            TablePotential tablaPConcepto = concepto.createDeltaTablePotential(0);
            tablaPConcepto.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesConcepto = probNet1.getPotentials(concepto).get(0).getVariables();
            tablaPConcepto.setVariables(variablesConcepto); // Establecer Variables
            tablaPConcepto.setValues(setValoresPriori()); // Establecer valores

            ProbNode nodo = new ProbNode(probNet1, concepto, NodeType.DECISION);
            nodo.setPotential(tablaPConcepto);
            probNet1.addProbNode(nodo);

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            inputStream.close();
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidStateException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param nombreTema Tema al que pertenece
     * @param preguntaT pregunta que se va a editar
     * @param nombreConceptos Lista de conceptos de la pregunta
     * @param numItems número de items de la pregunta
     */
    public void editarPregunta(String nombreTema, Pregunta preguntaT, List<Concepto> nombreConceptos, int numItems){
        int numConceptos = getNumConceptos(nombreTema);
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
            String nombrePregunta = preguntaT.getNombrePreg();
            Variable pregunta = probNet1.getVariable(nombrePregunta);

            /**
             * Potencial Pregunta
             */
            TablePotential tablaPPregunta = pregunta.createDeltaTablePotential(0);
            tablaPPregunta.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaV = new ArrayList<>();
            listaV.add(pregunta); // Variable Condicionada
            Variable concepto; // Variable Unidad (Hijo)
            for (Concepto nombreConcepto : nombreConceptos) {
                concepto = probNet1.getVariable(nombreConcepto.getNombreConcepto()); // Variable Unidad (Hijo)
                listaV.add(concepto);
            }
            tablaPPregunta.setVariables(listaV); // Establecer Variables
            tablaPPregunta.setValues(setValoresCondicionante(listaV, numItems, preguntaT.getTipopregunta().getDificultad(), preguntaT.getTipopregunta().getFdescuido(), preguntaT.getTipopregunta().getIndiceDis(), numConceptos)); // Establecer valores

            ProbNode nodo = new ProbNode(probNet1, pregunta, NodeType.CHANCE);
            nodo.setPotential(tablaPPregunta);
            probNet1.addProbNode(nodo);

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            inputStream.close();
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidStateException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 
     * @param nombreTema Tema al que pertenece
     * @param conceptoEliminar Concepto que se va a eliminar
     * @param preguntasConcepto Lista de preguntas relacionadas con el concepto
     * @param preguntasItems key = idPregunta, value = número de items de la pregunta
     */
    public void eliminarConcepto(String nombreTema, Concepto conceptoEliminar, List<Pregunta>preguntasConcepto, HashMap<Integer, Integer> preguntasItems){
        int numConceptos = getNumConceptos(nombreTema);
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();

            Variable concepto = probNet1.getVariable(conceptoEliminar.getNombreConcepto());
            System.out.println("OBTIENE LA VARIABLE CONCEPTO CORRECTAMENTE");
            Variable pregunta;
            Pregunta p;
            ProbNode nodo;
            for (Pregunta preguntasConcepto1 : preguntasConcepto) {
                p = (Pregunta) preguntasConcepto1;
                pregunta = probNet1.getVariable(p.getNombrePreg());
                System.out.println("OBTIENE LA PREGUNTA "+pregunta.getName() + " CORRECTAMENTE");
                probNet1.removeLink(concepto, pregunta, true); //eliminar el link entre el concepto y el pregunta
                System.out.println("REMUEVE EL LINK DEL CONCEPTO CON ESA PREGUNTA");
                /**
                 * Potencial Pregunta
                 */
                TablePotential tablaPPregunta = pregunta.createDeltaTablePotential(0);
                tablaPPregunta.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
                List<Variable> variablesPregunta = probNet1.getPotentials(pregunta).get(0).getVariables();
                System.out.println("OBTIENE LA LISTA DE POTENCIALES PARA ESA PREGUNTA");
                variablesPregunta.remove(concepto); // eliminamos el concepto de la tabla de potenciales
                System.out.println("REMUEVE LA VARIABLE CONCEPTO DE LA LISTA DE POTENCIALES");
                tablaPPregunta.setVariables(variablesPregunta); // Establecer Variables
                if (variablesPregunta.size() == 1) {
//                    tablaPPregunta.setValues(setValoresPriori()); // Establecer valores
                    probNet1.removeProbNode(probNet1.getProbNode(pregunta)); // eliminamos el nodo de la pregunta
                    System.out.println("LA PREGUNTA NO SE RELACIONA CON NADIE MÁS. ELIMINA PREGUNTA");
                } else {
                    tablaPPregunta.setValues(setValoresCondicionante(variablesPregunta, preguntasItems.get(p.getIdPregunta()), p.getTipopregunta().getDificultad(), p.getTipopregunta().getFdescuido(), p.getTipopregunta().getIndiceDis(), numConceptos)); // Establecer valores
                    System.out.println("LA PREGUNTA TIENE OTROS CONCEPTOS. RECALCULA VALORES");
                    nodo = new ProbNode(probNet1, pregunta, NodeType.CHANCE);
                    nodo.setPotential(tablaPPregunta);
                    probNet1.addProbNode(nodo); // Cambios en el nodo concepto
                    System.out.println("MODIFICA NODO PREGUNTA");
                }
            }

            probNet1.removeProbNode(probNet1.getProbNode(concepto)); // eliminamos el nodo del concepto
            System.out.println("REMUEVE EL NODO CONCEPTO");
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            System.out.println("FIN!!");
            inputStream.close();
        } catch (FileNotFoundException | ParserException | ProbNodeNotFoundException | WriterException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidStateException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminarPregunta(String nombreTema, Pregunta p){
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
            Variable pregunta = probNet1.getVariable(p.getNombrePreg());

            probNet1.removeProbNode(probNet1.getProbNode(pregunta)); // eliminamos el nodo del pregunta

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            inputStream.close();
        } catch (FileNotFoundException | ParserException | ProbNodeNotFoundException | WriterException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminarTema(String nombreTema) {
        String file = ruta + "/" + nombreTema + ".pgmx";
        File archivoPgmx = new File(file);
        if (archivoPgmx.delete()) {
            System.out.println("El fichero ha sido borrado satisfactoriamente");
        } else {
            System.out.println("El fichero no puede ser borrado");
        }
    }
    
    public void terminarInferencia(String nombreTema, String idSesion){
        eliminarTema(nombreTema+idSesion);
    }

    public void editarTema(String nombreTema, String nuevoNombre) {
        File archivoPgmx = new File(ruta + "/" + nombreTema + ".pgmx");
        File archivoPgmxNew = new File(ruta + "/" + nuevoNombre + ".pgmx");
        if (archivoPgmx.renameTo(archivoPgmxNew)) {
            System.out.println("El fichero ha sido renombrado correctamente");
        } else {
            System.out.println("El fichero no puede ser renombrado");
        }
    }
    
    public boolean existPregunta(String nombreTema, String nombrePregunta){
        InputStream inputStream = null;
        try {
            String file = ruta + "/" + nombreTema + ".pgmx";
            File archivoPgmx = new File(file);
            inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
            if(probNet1.containsVariable(nombrePregunta))
                return true;
            return false;
        } catch (FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } 
    }

    private String crearCopia(String nombreTema, String idSesion, HashMap<String, String> valueConceptos) {
        FileInputStream in = null;
        String rutaFinal = "";
        try {
            rutaFinal = ruta + "/" + nombreTema + idSesion + ".pgmx";
            File inFile = new File(ruta + "/" + nombreTema + ".pgmx");
            File outFile = new File(rutaFinal);
            in = new FileInputStream(inFile);
            FileOutputStream out = new FileOutputStream(outFile);
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            in.close();
            out.close();
            refrescarRed(idSesion, nombreTema, valueConceptos);

        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rutaFinal;
    }

    private String refrescarRed(String idSesion, String nombreTema, HashMap<String, String> nuevosValores) {
        List<Variable> lstVariableConcepto = getVariablesConceptos(nombreTema);
        try {
            String file = ruta + "/" + nombreTema + idSesion + ".pgmx";
            File archivoPgmx = new File(file);
            
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();

            String nombreVariable;
            String valorPositivo;
            Variable concepto;
            TablePotential tablaPConcepto;
            for (int i = 0; i < lstVariableConcepto.size(); i++) {
                nombreVariable = lstVariableConcepto.get(i).getName();
                concepto = probNet1.getVariable(nombreVariable);

                /**
                 * Potencial Concepto
                 */
                tablaPConcepto = concepto.createDeltaTablePotential(0);
                tablaPConcepto.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
                List<Variable> variablesConcepto = probNet1.getPotentials(concepto).get(0).getVariables();
                tablaPConcepto.setVariables(variablesConcepto); // Establecer Variables
                valorPositivo = nuevosValores.get(nombreVariable);
                tablaPConcepto.setValues(setValoresConceptos(Double.parseDouble(valorPositivo))); // Establecer valores después de la inferencia

                ProbNode nodo = new ProbNode(probNet1, concepto, NodeType.DECISION);
                nodo.setPotential(tablaPConcepto);
                probNet1.addProbNode(nodo);
            }

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            inputStream.close();
            return file;
        } catch (FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidStateException | IOException | WriterException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     *
     * @param idSesion nombre de la sesión del estudiante (username)
     * @param nombreTema nombre del tema (Test) al que pertenece la pregunta
     * @param pregunta Pregunta que ha sido respondida
     * @param respuesta True = respuesta correcta , False = respuesta incorrecta
     * @param valueConceptos Valores del porcentaje de aprendizaje de los
     * conceptos del test
     * @return HashMap key =nombreVariableConcepto, value =
     * nivelAprendizajeConcepto
     */
    public HashMap<String, String> inferencia(String idSesion, String nombreTema, Pregunta pregunta, boolean respuesta, HashMap<String, String> valueConceptos) {

        try {
            // REFRESCAR LA RED!!!
            // UTILIZAR ESTA RED PARA REALIZAR LA INFERENCIA POSTERIOR
            String file = refrescarRed(idSesion, nombreTema, valueConceptos);
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet = pgmxReader.loadProbNet(inputStream, nombreTema + idSesion).getProbNet();

            String estadoResp;
            if (respuesta) {
                estadoResp = "sí";
            } else {
                estadoResp = "no";
            }
            //EvidenceCase
            EvidenceCase evidence = new EvidenceCase();
            evidence.addFinding(probNet, pregunta.getNombrePreg(), estadoResp);
            InferenceAlgorithm variableElimination = new VariableElimination(probNet);

            variableElimination.setPreResolutionEvidence(evidence);

            List<Variable> lstVariableConcepto = getVariablesConceptos(nombreTema+idSesion);

            HashMap<Variable, TablePotential> posteriorProbabilities = variableElimination.getProbsAndUtilities();
            printResults(evidence, lstVariableConcepto, posteriorProbabilities, probNet);
            HashMap<String, String> inferenciaConceptos = doInference(evidence, lstVariableConcepto, posteriorProbabilities, probNet);
            return inferenciaConceptos;
        } catch (ProbNodeNotFoundException | InvalidStateException | IncompatibleEvidenceException | NotEvaluableNetworkException | UnexpectedInferenceException | FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public void printResults(EvidenceCase evidence, List<Variable> variablesOfInterest, 
			         Map<Variable, TablePotential> posteriorProbabilities, ProbNet probNet) {
		// Print the findings
		System.out.println("Evidence:");
		for (Finding finding : evidence.getFindings()) {
			System.out.print("  " + finding.getVariable() + ": ");
			System.out.println(finding.getState());
		}
		// Print the posterior probability of the state "present" of each variable of interest
		System.out.println("Posterior probabilities: ");
		for (Variable variable : variablesOfInterest) {
			try {
                            double value;
                            Variable variable1 = probNet.getVariable(variable.getName());
                            TablePotential posteriorProbabilitiesPotential = posteriorProbabilities.get(variable1);
                            System.out.print("  " + variable1 + ": ");
                            int stateIndex = -1;
                            try {
                                stateIndex = variable1.getStateIndex("sí");
                                value = posteriorProbabilitiesPotential.values[stateIndex];
                                System.out.println(Util.roundedString(value, "0.001"));
                            } catch (InvalidStateException e) {
                                System.err.println("State \"sí\" not found for variable \""
                                        + variable1.getName() + "\".");
                                e.printStackTrace();
                            }
                        } catch (ProbNodeNotFoundException ex) {
				Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		System.out.println();
	}
    private HashMap<String, String> doInference(EvidenceCase evidence, List<Variable> variablesOfInterest, HashMap<Variable, TablePotential> posteriorProbabilities, ProbNet probNet) throws ProbNodeNotFoundException {
        // Imprimir la probabilidad posterior del estado "pasa" de cada variable de interés
        HashMap<String, String> valoresInferencia = new HashMap<>();
        String key = "";
        double value;
        TablePotential posteriorProbabilitiesPotential;
        for (Variable variable : variablesOfInterest) {
            Variable variable1 = probNet.getVariable(variable.getName());
            posteriorProbabilitiesPotential = posteriorProbabilities.get(variable1);
            key = variable.getName();

            int stateIndex = -1;
            try {
                stateIndex = variable.getStateIndex("sí");
                value = posteriorProbabilitiesPotential.values[stateIndex];

                // Agregar el valor calculado para el concepto al HashMap
                if(value<0.001){ // cuando el valor se aproxima a 1
                    value=0.001; // valor límite
                }else if(value>0.999){
                    value=0.999;
                }
                valoresInferencia.put(key, Util.roundedString(value, "0.000001"));
            } catch (InvalidStateException e) {
                System.err.println("State \"sí\" not found for variable \"" + variable.getName() + "\".");
                e.printStackTrace();
            }
        }

        return valoresInferencia;
    }

    private String getValueInf(Variable variablesOfInterest, HashMap<Variable, TablePotential> posteriorProbabilities, ProbNet probNet, String estado) throws ProbNodeNotFoundException {
        // Imprimir la probabilidad posterior del estado "pasa" de cada variable de interés
        String key = "";
        double value;
        TablePotential posteriorProbabilitiesPotential;
        Variable variable1 = probNet.getVariable(variablesOfInterest.getName());
        posteriorProbabilitiesPotential = posteriorProbabilities.get(variable1);

        int stateIndex = -1;
        String valorPositivo = null;
        try {
            stateIndex = variable1.getStateIndex(estado);
            value = posteriorProbabilitiesPotential.values[stateIndex];
            valorPositivo = Util.roundedString(value, "0.000001");
        } catch (InvalidStateException e) {
            System.err.println("State \""+estado+ "\" not found for variable \"" + variable1.getName() + "\".");
            e.printStackTrace();
        }

        return valorPositivo;
    }

    private String verValuePreg(String nombreTema, String idSesion, String nombrePregunta, String estado) throws InvalidStateException {
        try {
            String file = ruta + "/" + nombreTema + idSesion + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema + idSesion).getProbNet();

            Variable pregunta = probNet1.getVariable(nombrePregunta);
            InferenceAlgorithm variableElimination = new VariableElimination(probNet1);
            HashMap<Variable, TablePotential> posteriorProbabilities = variableElimination.getProbsAndUtilities();
            String probSi = getValueInf(pregunta, posteriorProbabilities, probNet1, estado);
            String estadoMayor;
            if (Double.parseDouble(probSi) > 0.5) {
                estadoMayor = "sí";
            } else {
                estadoMayor = "no";
            }
            inputStream.close();
            return estadoMayor;
        } catch (FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NotEvaluableNetworkException | IncompatibleEvidenceException | UnexpectedInferenceException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Pregunta getPreguntaOptima(String nombreTema, String idSesion, List<Pregunta> lstPreguntas, HashMap<String, String> valueConceptos) {
        InputStream inputStream;
        try {
            String file = ruta + "/" + nombreTema + idSesion + ".pgmx";
            File archivoPgmx = new File(file);
            if (!archivoPgmx.exists()) {
                crearCopia(nombreTema, idSesion, valueConceptos);
            }
            // Variables
            PGMXReader pgmxReader = new PGMXReader();
            Object[][] utilidadPregunta = new Object[2][lstPreguntas.size()];
            String estadoMayor;
            Variable pregunta;
            List<Variable> lstPadres;
            EvidenceCase evidence;

            HashMap<Variable, TablePotential> posteriorProbabilities;
            String probSi;
            double mayor;
            for (int i = 0; i < lstPreguntas.size(); i++) {
                inputStream = new FileInputStream(archivoPgmx);
                ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreTema).getProbNet();
                try {
                    estadoMayor = verValuePreg(nombreTema, idSesion, lstPreguntas.get(i).getNombrePreg(),"sí");
                    pregunta = probNet1.getVariable(lstPreguntas.get(i).getNombrePreg());
                    lstPadres = probNet1.getPotentials(pregunta).get(0).getVariables();
                    lstPadres.remove(0); // eliminamos la variable pregunta para tener solo los padres
                    // inferir cadena si o no en padres y obtener el U(P) max de la pregunta
                    double[] valoresInferidos = new double[lstPadres.size()];
                    evidence = new EvidenceCase();
                    for (int j = 0; j < lstPadres.size(); j++) {
                        evidence.addFinding(probNet1, lstPadres.get(j).getName(), estadoMayor);
                        InferenceAlgorithm variableElimination = new VariableElimination(probNet1);
                        variableElimination.setPreResolutionEvidence(evidence);
                        posteriorProbabilities = variableElimination.getProbsAndUtilities();
                        probSi = getValueInf(pregunta, posteriorProbabilities, probNet1, estadoMayor);
                        valoresInferidos[j] = Double.parseDouble(probSi);
                        evidence.removeFinding(lstPadres.get(j).getName());
                    }
                    // Obtener utilidad de la pregunta (mayor inferencia)
                    mayor = valoresInferidos[0];
                    for (int f = 1; f < valoresInferidos.length; f++) {
                        if (valoresInferidos[f] > mayor) {
                            mayor = valoresInferidos[f];
                        }
                    }
                    // Guardar utilidad de la pregunta
                    utilidadPregunta[0][i] = lstPreguntas.get(i);
                    utilidadPregunta[1][i] = mayor + "";
                    inputStream.close();
                } catch (InvalidStateException | ProbNodeNotFoundException | IncompatibleEvidenceException | UnexpectedInferenceException | NoFindingException ex) {
                    Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // retornar pregunta con mayor utilidad
            mayor = Double.parseDouble((String) utilidadPregunta[1][0]);
            double value;
            ArrayList<Integer> listaPos= new ArrayList<>();
            listaPos.add(0);
            for (int f = 1; f < lstPreguntas.size(); f++) {
                value = Double.parseDouble((String) utilidadPregunta[1][f]);
                if (value > mayor) {
                    mayor = value;
                    listaPos.clear();
                    listaPos.add(f);
                }else{
                    if(value==mayor){
                        listaPos.add(f);
                    }
                }
            }
            if(listaPos.size()==1){ // Cuando hay una sola pregunta óptima
                return (Pregunta) utilidadPregunta[0][listaPos.get(0)];
            }else{ // cuando hay varias preguntas óptimas
                int codigoPregunta =(int)(Math.random()*listaPos.size());
                return (Pregunta) utilidadPregunta[0][listaPos.get(codigoPregunta)];
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserException | IOException | NotEvaluableNetworkException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static void main(String[] args) {
//        try {
        Concepto conc = new Concepto(1, new Tema(), "Concepto", "traducción", true);
        Concepto conc1 = new Concepto(2, new Tema(), "Concepto1", "traducción", true);
//        
//        Pregunta pregunta = new Pregunta(1, "Enunciado", 0.02, 0.02, 0.02, "Pregunta", true);
  //      Pregunta pregunta1 = new Pregunta(2, "Enunciado", 0.02, 0.02, 0.02, "Pregunta1", true);
//        Pregunta pregunta3 = new Pregunta(3, test, conc, "Descripcion", 3, true); // Speak M
//        Pregunta pregunta4 = new Pregunta(4, test, conc, "Descripcion", 2, true); // Listen F
//        Pregunta pregunta5 = new Pregunta(5, test, conc, "Descripcion", 2, true); // Speak F
//        Pregunta pregunta6 = new Pregunta(6, test, conc2, "Descripcion", 4, true); // Speaking D
//        Pregunta pregunta7 = new Pregunta(7, test, conc2, "Descripcion", 3, true); // Listening M
//        Pregunta pregunta8 = new Pregunta(8, test, conc2, "Descripcion", 3, true); // Speak M
//        Pregunta pregunta9 = new Pregunta(9, test, conc3, "Descripcion", 4, true); // Speak D
//        Pregunta pregunta10 = new Pregunta(10, test, conc3, "Descripcion", 3, true); // Speak M
//        Pregunta pregunta11 = new Pregunta(11, test, conc3, "Descripcion", 2, true); // Speak F
//        Pregunta pregunta12 = new Pregunta(12, test, conc3, "Descripcion", 2, true); // Listen F
//
//        Item i = new Item(1, pregunta, "nombre", "img", "traducción", true);
//        Item i1 = new Item(2, pregunta, "nombre", "img", "traducción", true);
//        Item i2 = new Item(3, pregunta, "nombre", "img", "traducción", true);
//        Item i3 = new Item(4, pregunta, "nombre", "img", "traducción", true);
//        Item i4 = new Item(5, pregunta, "nombre", "img", "traducción", true);
//        Set<Item> setItem = new HashSet<>(); // Listen(F, M), Speak(F)
//        setItem.add(i);
//        setItem.add(i1);
//        setItem.add(i2);
//        setItem.add(i3);
//        setItem.add(i4);
//
//        Set<Item> setItem1 = new HashSet<>(); // Speak(M, D)
//        setItem1.add(i);
//
//        pregunta.setItems(setItem1); // Speak D
//        pregunta2.setItems(setItem); // List M
//        pregunta3.setItems(setItem1); // Speak M
//        pregunta4.setItems(setItem); // listen F
//        pregunta5.setItems(setItem); // Speak F
//        pregunta6.setItems(setItem1); // Speak D
//        pregunta7.setItems(setItem); // Listen M
//        pregunta8.setItems(setItem1); // Speak M
//        pregunta9.setItems(setItem1); // Speak D
//        pregunta10.setItems(setItem); // Listen M
//        pregunta11.setItems(setItem); // Speak F
//        pregunta12.setItems(setItem); // Listen F
//
        ArrayList<Concepto> lstConceptos = new ArrayList<>(); // List Concepto
        lstConceptos.add(conc);
        lstConceptos.add(conc1);
//
        ArrayList<Concepto> lstConceptos1 = new ArrayList<>(); // List Concepto 2
        lstConceptos1.add(conc);
//
//        ArrayList<String> lstConceptos2 = new ArrayList<>(); // List Concepto 3
//        lstConceptos2.add(conc3.getNombreConcepto());
//
//        ArrayList<String> lstConceptos3 = new ArrayList<>(); // TODOS
//        lstConceptos3.add(conc.getNombreConcepto());
//        lstConceptos3.add(conc2.getNombreConcepto());
//        lstConceptos3.add(conc3.getNombreConcepto());
//
//        ArrayList<String> lstConceptos4 = new ArrayList<>(); // CONCEPTO-CONCEPTO2
//        lstConceptos4.add(conc.getNombreConcepto());
//        lstConceptos4.add(conc2.getNombreConcepto());
//
//        ArrayList<String> lstConceptos5 = new ArrayList<>(); // CONCEPTO3-CONCEPTO2
//        lstConceptos5.add(conc3.getNombreConcepto());
//        lstConceptos5.add(conc2.getNombreConcepto());
//
        CrearBayesDynamic c = new CrearBayesDynamic();
        String nombreTema = "TemaControl";
//
        ArrayList<Pregunta> lstPreguntas = new ArrayList<>(); // preguntas Concepto3
//        lstPreguntas.add(pregunta);
  //      lstPreguntas.add(pregunta1);
//        lstPreguntas.add(pregunta3);
//        lstPreguntas.add(pregunta4);
//        lstPreguntas.add(pregunta5);
//        lstPreguntas.add(pregunta11);
//        
        ArrayList<Pregunta> lstPreguntas2 = new ArrayList<>(); // preguntas Concepto3
    //    lstPreguntas2.add(pregunta1);
//        lstPreguntas2.add(pregunta5);
//        lstPreguntas2.add(pregunta6);
//        lstPreguntas2.add(pregunta7);
//        lstPreguntas2.add(pregunta8);
//        lstPreguntas2.add(pregunta11);
//        lstPreguntas2.add(pregunta12);
//        
//        ArrayList<Pregunta> lstPreguntas3 = new ArrayList<>(); // preguntas Concepto3
//        lstPreguntas3.add(pregunta4);
//        lstPreguntas3.add(pregunta9);
//        lstPreguntas3.add(pregunta10);
//        lstPreguntas3.add(pregunta11);
//        lstPreguntas3.add(pregunta12);
//
        HashMap<Integer, Integer> items= new HashMap<>();
        items.put(1, 2);
        items.put(2, 1);
        c.eliminarConcepto(nombreTema, conc, lstPreguntas, items);
//            c.eliminarConcepto(nombreTema, conc2, lstPreguntas);
//            c.eliminarPregunta(nombreTema, pregunta7);
//        HashMap<String, String> valoresIniciales = new HashMap<>();
//        valoresIniciales.put("Concepto", "0.08");
//        valoresIniciales.put("Concepto2", "0.08");
//        valoresIniciales.put("Concepto3", "0.08");
//
//            c.crearRedTema(nombreTema);
//            c.crearConcepto(nombreTema, conc.getNombreConcepto());
//            c.crearConcepto(nombreTema, conc1.getNombreConcepto());
////            c.crearConcepto(nombreTema, conc2.getNombreConcepto());
////
//            c.crearPregunta(nombreTema, pregunta, lstConceptos, 2); // Speaking Difícil, descuido bajo, id alto
//            c.crearPregunta(nombreTema, pregunta1, lstConceptos1, 1); // LISTENING INTERMEDIO Descuido medio, iD medio
////            c.crearPregunta(nombreTema, pregunta3, lstConceptos, 0.001, 1.2); // Speaking medio Descuido bajo, id medio
////            c.crearPregunta(nombreTema, pregunta4, lstConceptos3, 0.2, 0.2); // Listening FÁCIL Descuido alto, indice de discriminación bajo
////            c.crearPregunta(nombreTema, pregunta5, lstConceptos4, 0.001, 0.2);  // Speaking Fácil descuido bajo, id bajo
////            c.crearPregunta(nombreTema, pregunta6, lstConceptos1, 0.001, 2.0); // Speaking Difícil, descuido bajo, id alto
////            c.crearPregunta(nombreTema, pregunta7, lstConceptos1, 0.01, 1.2); // LISTENING INTERMEDIO Descuido medio, iD medio
////            c.crearPregunta(nombreTema, pregunta8, lstConceptos1, 0.001, 1.2); // Speaking medio Descuido bajo, id medio
////            c.crearPregunta(nombreTema, pregunta9, lstConceptos2, 0.001, 2.0); // Speaking Difícil
////            c.crearPregunta(nombreTema, pregunta10, lstConceptos2, 0.01, 1.2); // LISTENING INTERMEDIO Descuido medio, iD medio
////            c.crearPregunta(nombreTema, pregunta11, lstConceptos3, 0.001, 0.2);  // Speaking Fácil descuido bajo, id bajo
////            c.crearPregunta(nombreTema, pregunta12, lstConceptos5, 0.2, 0.2); // Listening Fácil
//        String sesion= "Est001";
//        Pregunta pOptima = c.getPreguntaOptima(nombreTema, sesion, conc3, lstPreguntas3);
//        System.out.println("Pregunta óptima " + pOptima.getIdPregunta());
//        HashMap<String, String> valorPosteriori = c.inferencia(sesion, nombreTema, pOptima, true, valoresIniciales);
//        System.out.println(valorPosteriori);
//        
//        pOptima = c.getPreguntaOptima(nombreTema, sesion, conc2, lstPreguntas2);
//        System.out.println("Pregunta óptima " + pOptima.getIdPregunta());
//        valorPosteriori = c.inferencia(sesion, nombreTema, pOptima, true, valorPosteriori);
//        System.out.println(valorPosteriori);
//        
//        pOptima = c.getPreguntaOptima(nombreTema, sesion, conc2, lstPreguntas2);
//        System.out.println("Pregunta óptima " + pOptima.getIdPregunta());
//        valorPosteriori = c.inferencia(sesion, nombreTema, pOptima, true, valorPosteriori);
//        System.out.println(valorPosteriori);
//
//        pOptima = c.getPreguntaOptima(nombreTema, sesion, conc, lstPreguntas);
//        System.out.println("Pregunta óptima " + pOptima.getIdPregunta());
//        valorPosteriori = c.inferencia(sesion, nombreTema, pOptima, true, valorPosteriori);
//        System.out.println(valorPosteriori);
//
////        } catch (InvalidStateException ex) {
////            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (ConstraintViolationException ex) {
////            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
////        }
    }
}
