/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases.RedBayesiana;

import Pojo.Resultado;
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
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.ParserException;
import org.openmarkov.core.exception.ProbNodeNotFoundException;
import org.openmarkov.core.exception.UnexpectedInferenceException;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.inference.InferenceAlgorithm;
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
 * @author Usuario
 */
public class CrearBayesNetwork1 {

//    final private String bayesNetworkName = "BayesNetworkIngles1.pgmx";
    final private double valorNo = 0.05;
    private State[] estados = new State[2];
    private String ruta;

    public CrearBayesNetwork1() {
        /**
         * Se define los estados
         */
        estados[0] = new State("sí");
        estados[1] = new State("no");
        ServletContext servletContex= (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        this.ruta= (String) servletContex.getRealPath("/resources/RedesBayesianas/BNEvaluate");
//        this.ruta= "C:\\Users\\KathyR\\Dropbox\\Décimo\\Trabajo de titulación\\Red bayesiana";
    }

    /**
     *
     * @param listaVariables = variables que forman parte del TablePotential
     * @return valores de los estados donde la nueva variable es la
     * condicionante
     */
    private double[] setValoresCondicionante(List<Variable> listaVariables) {
        //Asignación de valores
        int contador = 0;
        double puntaje = (double) 1.0 / (listaVariables.size() - 1);
        int longitud = listaVariables.size();
        double[] valores = new double[(int) Math.pow(2, longitud)];
        String numBin = "";
        for (int i = 0; i < (int) Math.pow(2, longitud); i++) {
            if (i % 2 == 0) {
                numBin = Integer.toBinaryString(i);
                StringBuilder builder = new StringBuilder(numBin);
                String numBinInvertida = builder.reverse().toString();
                while (numBinInvertida.length() < longitud) {
                    numBinInvertida = numBinInvertida + 0;
                }
                for (int x = 0; x < numBin.length(); x++) {
                    if ((numBin.charAt(x) == '1')) {
                        contador++;
                    }
                }
//                    System.out.println(numBinInvertida);
                if ((contador * puntaje) == 1.0) {
                    valores[i] = 1 - (contador * puntaje) + valorNo; // Valor 0 Máximo
                } else {
                    valores[i] = 1 - (contador * puntaje) - valorNo; // Valor 0
                    }
                contador = 0;
            } else {
                valores[i] = 1 - valores[i - 1]; // Valor 1
            }
        }
        return valores;
    }

    /**
     *
     * @return valores de los estados donde la nueva variable es la condicionada
     */
    private double[] setValoresCondicionada() {
        double[] valores = new double[2]; // dos estados sí y no
        valores[0] = valorNo; // valor 1 
        valores[1] = 1 - valorNo; // valor 0 
        return valores;
    }
    
    private double[] setvalueInferConcepto(double valorSi) {
        double[] valores = new double[2]; // dos estados sí y no
        valores[0] = valorSi; // valor 1 
        valores[1] = 1 - valorSi; // valor 0 
        return valores;
    }

    /**
     *
     * @param nombreUnidad String nombre de la Unidad a crear
     * @throws InvalidStateException
     */
    public void crearUnidad(String nombreUnidad) throws InvalidStateException {
        try {
//            String file = new File(".").getCanonicalPath() + "/" + bayesNetworkName;
            String file = ruta +"/" +nombreUnidad + ".pgmx";
            ProbNet probNet1 = new ProbNet(org.openmarkov.core.model.network.type.BayesianNetworkType.getUniqueInstance());
            probNet1.setNetworkType(new NetworkTypeManager().getNetworkType("BayesianNetwork"));


            /**
             * CREACIÓN DE VARIABLES
             */
            Variable unidad = new Variable(nombreUnidad);
            unidad.setVariableType(VariableType.FINITE_STATES);
            unidad.setStates(estados);

            /**
             * Potencial Unidad
             */
            TablePotential tablaPUnidad = unidad.createDeltaTablePotential(0);
            tablaPUnidad.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaVariablesUnidad = new ArrayList<>();
            listaVariablesUnidad.add(unidad); // Variable condicionada
            tablaPUnidad.setVariables(listaVariablesUnidad); // Establecer Variables
            tablaPUnidad.setValues(setValoresCondicionada()); // Establecer valores

            /**
             * Crear nodos
             */
            ProbNode nodo = new ProbNode(probNet1, unidad, NodeType.CHANCE);
            nodo.setPotential(tablaPUnidad);
            probNet1.addProbNode(nodo);

            /**
             * Escritura de la red
             */
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (WriterException | ConstraintViolationException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param nombreUnidad Unidad a la que pertenece el tema
     * @param nombreTema Nombre del Tema a crear
     * @throws InvalidStateException
     */
    public void crearTema(String nombreUnidad, String nombreTema) throws InvalidStateException, ConstraintViolationException {
        try {
            String file = ruta +"\\" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();

            /**
             * CREACIÓN DE VARIABLES
             */
            Variable tema = new Variable(nombreTema); // Variable tema
            tema.setVariableType(VariableType.FINITE_STATES);
            tema.setStates(estados);

            Variable unidad = probNet1.getVariable(nombreUnidad); // Variable Unidad (Hijo)

            //Potencial Unidad
            TablePotential tablaPUnidad = unidad.createDeltaTablePotential(0);
            tablaPUnidad.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaVariablesUnidad = probNet1.getPotentials(unidad).get(0).getVariables();
            listaVariablesUnidad.add(tema); // Variable condicionante
            tablaPUnidad.setVariables(listaVariablesUnidad); // Establecer Variables
            tablaPUnidad.setValues(setValoresCondicionante(listaVariablesUnidad));

            //Potencial Tema
            TablePotential tablaPTema = tema.createDeltaTablePotential(0); // Variable hijo
            tablaPTema.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaV = new ArrayList<>();
            listaV.add(tema); // Variable Condicionada
            tablaPTema.setVariables(listaV); // Establecer Variables
            tablaPTema.setValues(setValoresCondicionada()); // Establecer valores

            /**
             * Crear nodos
             */
            ProbNode nodo = new ProbNode(probNet1, unidad, NodeType.CHANCE);
            nodo.setPotential(tablaPUnidad);
            probNet1.addProbNode(nodo);

            ProbNode nodo1 = new ProbNode(probNet1, tema, NodeType.CHANCE);
            nodo1.setPotential(tablaPTema);
            probNet1.addProbNode(nodo1);

            /**
             * Escritura de la red
             */
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param nombreUnidad Unidad a la que pertenece el Tema
     * @param nombreTema Tema al que pertenece el Concepto
     * @param nombreConcepto Nombre del Concepto a crear
     * @throws InvalidStateException
     */
    public void crearConcepto(String nombreUnidad, String nombreTema, String nombreConcepto) throws InvalidStateException {
        try {
//            String file = new File(".").getCanonicalPath() + "/" + bayesNetworkName;
           String file = ruta +"\\" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();

            /**
             * CREACIÓN DE VARIABLES
             */
            Variable concepto = new Variable(nombreConcepto); // Nueva variable tema
            concepto.setVariableType(VariableType.FINITE_STATES);
            concepto.setStates(estados);

            Variable tema = probNet1.getVariable(nombreTema); // Variable Tema (Hijo)

            /**
             * Potencial Tema
             */
            TablePotential tablaPTema = tema.createDeltaTablePotential(0);
            tablaPTema.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesTema = probNet1.getPotentials(tema).get(0).getVariables();
            variablesTema.add(concepto); // Variable condicionante
            tablaPTema.setVariables(variablesTema); // Establecer Variables
            tablaPTema.setValues(setValoresCondicionante(variablesTema));

            /**
             * Potencial Concepto
             */
            TablePotential tablaPConcepto = concepto.createDeltaTablePotential(0); // Variable hijo
            tablaPConcepto.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesConcepto = new ArrayList<>();
            variablesConcepto.add(concepto); // Variable Condicionada
            tablaPConcepto.setVariables(variablesConcepto); // Establecer Variables
            tablaPConcepto.setValues(setValoresCondicionada()); // Establecer valores

            /**
             * Crear nodos
             */
            ProbNode nodo = new ProbNode(probNet1, tema, NodeType.CHANCE);
            nodo.setPotential(tablaPTema);
            probNet1.addProbNode(nodo);

            ProbNode nodo1 = new ProbNode(probNet1, concepto, NodeType.CHANCE);
            nodo1.setPotential(tablaPConcepto);
            probNet1.addProbNode(nodo1);

            /**
             * Escritura de la red
             */
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void editarUnidad(String nombreUnidad, String nuevoNombre) throws InvalidStateException {
        try {
            String file = ruta +"/" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            ProbNet probNet1;
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();

            Variable unidad = probNet1.getVariable(nombreUnidad);
            unidad.setName(nuevoNombre);

            /**
             * Potencial Unidad
             */
            TablePotential tablaPUnidad = unidad.createDeltaTablePotential(0);
            tablaPUnidad.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaVariablesUnidad = probNet1.getPotentials(unidad).get(0).getVariables();
            tablaPUnidad.setVariables(listaVariablesUnidad); // Establecer Variables
            if (listaVariablesUnidad.size() == 1) {
                tablaPUnidad.setValues(setValoresCondicionada()); // Establecer valores
            } else {
                tablaPUnidad.setValues(setValoresCondicionante(listaVariablesUnidad)); // Establecer valores
            }

            ProbNode nodo = new ProbNode(probNet1, unidad, NodeType.CHANCE);
            nodo.setPotential(tablaPUnidad);
            probNet1.addProbNode(nodo);

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
            archivoPgmx.renameTo(new File(ruta + "/"+nuevoNombre + ".pgmx"));
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param nombreUnidad Unidad en la que se encuentra el tema
     * @param nombreTema Nombre del tema
     * @param nuevoNombre Nombre al que se quiere renombrar el Tema
     * @throws InvalidStateException
     */
    public void editarTema(String nombreUnidad, String nombreTema, String nuevoNombre) throws InvalidStateException {
        try {
            String file = ruta +"/" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();

            Variable tema = probNet1.getVariable(nombreTema);
            tema.setName(nuevoNombre);

            /**
             * Potencial Unidad
             */
            TablePotential tablaPTema = tema.createDeltaTablePotential(0);
            tablaPTema.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> listaVariablesTema = probNet1.getPotentials(tema).get(0).getVariables();
            tablaPTema.setVariables(listaVariablesTema); // Establecer Variables
            if (listaVariablesTema.size() == 1) {
                tablaPTema.setValues(setValoresCondicionada()); // Establecer valores
            } else {
                tablaPTema.setValues(setValoresCondicionante(listaVariablesTema)); // Establecer valores
            }

            ProbNode nodo = new ProbNode(probNet1, tema, NodeType.CHANCE);
            nodo.setPotential(tablaPTema);
            probNet1.addProbNode(nodo);

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void editarConcepto(String nombreUnidad, String nombreConcepto, String nuevoNombre) throws InvalidStateException {
        try {
            String file = ruta +"/" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();

            Variable concepto = probNet1.getVariable(nombreConcepto);
            concepto.setName(nuevoNombre);

            /**
             * Potencial Concepto
             */
            TablePotential tablaPConcepto = concepto.createDeltaTablePotential(0);
            tablaPConcepto.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesConcepto = probNet1.getPotentials(concepto).get(0).getVariables();
            tablaPConcepto.setVariables(variablesConcepto); // Establecer Variables
            if (variablesConcepto.size() == 1) {
                tablaPConcepto.setValues(setValoresCondicionada()); // Establecer valores
            } else {
                tablaPConcepto.setValues(setValoresCondicionante(variablesConcepto)); // Establecer valores
            }

            ProbNode nodo = new ProbNode(probNet1, concepto, NodeType.CHANCE);
            nodo.setPotential(tablaPConcepto);
            probNet1.addProbNode(nodo);

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (WriterException | FileNotFoundException | ParserException | ProbNodeNotFoundException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminarConcepto(String nombreUnidad, String nombreTema, String nombreConcepto) throws InvalidStateException {
        try {
            String file = ruta +"/" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();

            Variable tema = probNet1.getVariable(nombreTema);
            Variable concepto = probNet1.getVariable(nombreConcepto);

            probNet1.removeLink(concepto, tema, true); //eliminar el link entre el unidad y el tema
            /**
             * Potencial Concepto
             */
            TablePotential tablaPTema = tema.createDeltaTablePotential(0);
            tablaPTema.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesTema = probNet1.getPotentials(tema).get(0).getVariables();
            variablesTema.remove(concepto); // eliminamos el tema de la tabla de potenciales
            tablaPTema.setVariables(variablesTema); // Establecer Variables
            if (variablesTema.size() == 1) {
                tablaPTema.setValues(setValoresCondicionada()); // Establecer valores
            } else {
                tablaPTema.setValues(setValoresCondicionante(variablesTema)); // Establecer valores
            }

            ProbNode nodo = new ProbNode(probNet1, tema, NodeType.CHANCE);
            nodo.setPotential(tablaPTema);
            probNet1.addProbNode(nodo); // Cambios en el nodo unidad
            probNet1.removeProbNode(probNet1.getProbNode(concepto)); // eliminamos el nodo del tema

            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);
        } catch (FileNotFoundException | ParserException | ProbNodeNotFoundException | WriterException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminarTema(String nombreUnidad, String nombreTema) throws InvalidStateException {
        try {
            String file = ruta +"/" +nombreUnidad + ".pgmx";
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();
            Variable unidad = probNet1.getVariable(nombreUnidad);
            Variable tema = probNet1.getVariable(nombreTema);

            probNet1.removeLink(tema, unidad, true); //eliminar el link entre el unidad y el tema
            /**
             * Potencial Concepto
             */
            TablePotential tablaPUnidad = unidad.createDeltaTablePotential(0);
            tablaPUnidad.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
            List<Variable> variablesUnidad = probNet1.getPotentials(unidad).get(0).getVariables();
            variablesUnidad.remove(tema); // eliminamos el tema de la tabla de potenciales
            tablaPUnidad.setVariables(variablesUnidad); // Establecer Variables
            if (variablesUnidad.size() == 1) {
                tablaPUnidad.setValues(setValoresCondicionada()); // Establecer valores
            } else {
                tablaPUnidad.setValues(setValoresCondicionante(variablesUnidad)); // Establecer valores
            }

            ProbNode nodo = new ProbNode(probNet1, unidad, NodeType.CHANCE);
            nodo.setPotential(tablaPUnidad);
            probNet1.addProbNode(nodo); // Cambios en el nodo unidad

            // Eliminación de conceptos
            List<Variable> variablesTema = probNet1.getPotentials(tema).get(0).getVariables();
            Variable concepto = null;
            ProbNode nodo1 = null;
            if (variablesTema.size() > 1) {
                for (int i = 1; i < variablesTema.size(); i++) {
                    concepto = variablesTema.get(i);
                    probNet1.removeLink(concepto, tema, true); //eliminar el link entre el unidad y el tema

                    TablePotential tablaPTema = tema.createDeltaTablePotential(0);
                    tablaPTema.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
                    List<Variable> variablesTema1 = probNet1.getPotentials(tema).get(0).getVariables();
                    variablesTema1.remove(concepto); // eliminamos el tema de la tabla de potenciales
                    tablaPTema.setVariables(variablesTema1); // Establecer Variables
                    if (variablesTema1.size() == 1) {
                        tablaPTema.setValues(setValoresCondicionada()); // Establecer valores
                    } else {
                        tablaPTema.setValues(setValoresCondicionante(variablesTema1)); // Establecer valores
                    }

                    nodo1 = new ProbNode(probNet1, tema, NodeType.CHANCE);
                    nodo1.setPotential(tablaPTema);
                    probNet1.addProbNode(nodo1); // Cambios en el nodo unidad
                    probNet1.removeProbNode(probNet1.getProbNode(concepto)); // eliminamos el nodo del tema
                }
            }
            probNet1.removeProbNode(probNet1.getProbNode(tema)); // eliminamos el nodo del tema
            PGMXWriter pgmxWriter = new PGMXWriter();
            pgmxWriter.writeProbNet(file, probNet1);

        } catch (FileNotFoundException | ParserException | ProbNodeNotFoundException | WriterException ex) {
            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminarUnidad(String nombreUnidad) {
        String file = ruta +"/" +nombreUnidad + ".pgmx";
        File archivoPgmx = new File(file);
        if (archivoPgmx.delete()) {
            System.out.println("El fichero ha sido borrado satisfactoriamente");
        } else {
            System.out.println("El fichero no puede ser borrado");
        }
    }
    
    private String crearCopia(String nombreUnidad, String idSesion) {
        FileInputStream in = null;
        String rutaFinal="";
        try {
            rutaFinal=ruta + "/" + nombreUnidad + idSesion+ ".pgmx";
            File inFile = new File(ruta + "/" + nombreUnidad +".pgmx");
            File outFile = new File(rutaFinal);
            in = new FileInputStream(inFile);
            FileOutputStream out = new FileOutputStream(outFile);
            int c;
            while ((c = in.read()) != -1) {
                out.write(c);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
                return rutaFinal;
            } catch (IOException ex) {
                Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    private String refrescarRed(String idSesion, String nombreUnidad, List<Resultado> resultados) {
        try {
            String file = ruta + "/" + nombreUnidad + idSesion+ ".pgmx";
            File archivoPgmx = new File(file);
            if(!archivoPgmx.exists()){
                crearCopia(nombreUnidad, idSesion);
            }
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet1 = pgmxReader.loadProbNet(inputStream, nombreUnidad).getProbNet();
            String nombreVariable;
            double valorPositivo;
            Variable concepto;
            TablePotential tablaPConcepto;
            for (int i = 0; i < resultados.size(); i++) {
                nombreVariable = resultados.get(i).getConcepto().getNombreConcepto();
                concepto = probNet1.getVariable(nombreVariable);

                tablaPConcepto = concepto.createDeltaTablePotential(0);
                tablaPConcepto.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY); // Rol
                List<Variable> variablesConcepto = probNet1.getPotentials(concepto).get(0).getVariables();
                tablaPConcepto.setVariables(variablesConcepto); // Establecer Variables
                valorPositivo = resultados.get(i).getValor();
                tablaPConcepto.setValues(setvalueInferConcepto(valorPositivo)); // Establecer valores después de la inferencia

                ProbNode nodo = new ProbNode(probNet1, concepto, NodeType.CHANCE);
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

    
    public HashMap<String, String> inferencia(String idSesion, String nombreUnidad, List<Resultado> resultados) {
        try {
            // REFRESCAR LA RED!!!
            // UTILIZAR ESTA RED PARA REALIZAR LA INFERENCIA POSTERIOR
            String file = refrescarRed(idSesion, nombreUnidad, resultados);
            File archivoPgmx = new File(file);
            InputStream inputStream = new FileInputStream(archivoPgmx);
            PGMXReader pgmxReader = new PGMXReader();
            ProbNet probNet = pgmxReader.loadProbNet(inputStream, nombreUnidad + idSesion).getProbNet();

            List<Variable> lstVariableConcepto = probNet.getVariables();
            for (int i = 0; i < resultados.size(); i++) {
                lstVariableConcepto.remove(probNet.getVariable(resultados.get(i).getConcepto().getNombreConcepto()));
            }
            
            InferenceAlgorithm variableElimination = new VariableElimination(probNet);
            HashMap<Variable, TablePotential> posteriorProbabilities = variableElimination.getProbsAndUtilities();
            printResults(lstVariableConcepto, posteriorProbabilities, probNet);
            HashMap<String, String> inferenciaConceptos = doInference(lstVariableConcepto, posteriorProbabilities, probNet);
            // ELIMINAR LA RED TEMPORAL CORRESPONDIENTE A LA SESIÓN CONSULTADA
            eliminarUnidad(nombreUnidad+idSesion);
            return inferenciaConceptos;
        } catch (ProbNodeNotFoundException | IncompatibleEvidenceException | NotEvaluableNetworkException | UnexpectedInferenceException | FileNotFoundException | ParserException ex) {
            Logger.getLogger(CrearBayesDynamic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
public void printResults(List<Variable> variablesOfInterest, 
			         Map<Variable, TablePotential> posteriorProbabilities, ProbNet probNet) {
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
    private HashMap<String, String> doInference(List<Variable> variablesOfInterest, HashMap<Variable, TablePotential> posteriorProbabilities, ProbNet probNet) throws ProbNodeNotFoundException {
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
//            try {
//                stateIndex = variable.getStateIndex("sí");
                value = posteriorProbabilitiesPotential.values[0];

                // Agregar el valor calculado para el concepto al HashMap
                valoresInferencia.put(key, Util.roundedString(value, "0.0001"));
//            } catch (InvalidStateException e) {
//                System.err.println("State \"sí\" not found for variable \"" + variable.getName() + "\".");
//                e.printStackTrace();
//            }
        }

        return valoresInferencia;
    }


    public static void main(String[] args) {
//        try {
//            CrearBayesNetwork1 c = new CrearBayesNetwork1();
//            String nombreUnidad="UnidadProb";
//            c.crearUnidad(nombreUnidad);
//            c.crearTema(nombreUnidad, "Tema1");
//            c.crearTema(nombreUnidad, "Tema2");
//            c.crearConcepto(nombreUnidad, "Tema1", "Concepto");
//            c.crearConcepto(nombreUnidad, "Tema1", "Concepto2");
//            c.crearConcepto(nombreUnidad, "Tema1", "Concepto3");
//            c.crearConcepto(nombreUnidad, "Tema1", "Concepto4");
////            
//            c.crearConcepto(nombreUnidad, "Tema2", "Concepto5");
//            c.crearConcepto(nombreUnidad, "Tema2", "Concepto6");
//            
//            Estudiante est= new Estudiante();
//            
//            
////            Resultado1 r= new Resultado1(1, est, new Concepto(1, new Tema(), "Concepto3", "traducción", "descripción", true), 0.317);
////            Resultado1 r1= new Resultado1(1, est, new Concepto(1, new Tema(), "Concepto4", "traducción", "descripción", true), 0.317);
////            Resultado1 r2= new Resultado1(1, est, new Concepto(1, new Tema(), "Concepto2", "traducción", "descripción", true), 0.695);
////            Resultado1 r3= new Resultado1(1, est, new Concepto(1, new Tema(), "Concepto", "traducción", "descripción", true), 0.831);
////            Resultado1 r4= new Resultado1(1, est, new Concepto(1, new Tema(), "Concepto5", "traducción", "descripción", true), 0.05);
////            Resultado1 r5= new Resultado1(1, est, new Concepto(1, new Tema(), "Concepto6", "traducción", "descripción", true), 0.05);
////            List<Resultado1> resultados= new ArrayList<>();
////            resultados.add(r);
////            resultados.add(r1);
////            resultados.add(r2);
////            resultados.add(r3);
////            resultados.add(r4);
////            resultados.add(r5);
////            
////            HashMap<String, String> valorPosteriori=c.inferencia("Est001", nombreUnidad, resultados);
////            System.out.println(valorPosteriori);        
//        } catch (InvalidStateException ex) {
//            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ConstraintViolationException ex) {
//            Logger.getLogger(CrearBayesNetwork1.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

}
