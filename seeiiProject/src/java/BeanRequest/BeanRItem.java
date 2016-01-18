/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package BeanRequest;

import Clases.RedBayesiana.CrearBayesDynamic;
import Dao.DaoConcepto;
import Dao.DaoItem;
import Dao.DaoPregConc;
import Dao.DaoPregunta;
import HibernateUtil.HibernateUtil;
import Pojo.Concepto;
import Pojo.Item;
import Pojo.PregConc;
import Pojo.Pregunta;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author KathyR
 */
@ManagedBean
@ViewScoped
public class BeanRItem {

    private Item item;
    private Pregunta pregunta;
    private List<Item> listaItem;
    private Session session;
    private Transaction transaction;
    private String nombreImagen;
    private byte[] contenidoImg;
    private UploadedFile imagen;

    //constructor
    public BeanRItem() {
        this.item = new Item();
    }

    public void registrar(Pregunta pregunta) {
        this.session = null;
        this.transaction = null;
        try {
            actualizarImg();
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR DE LECTURA ESCRITURA:", "Contacte con el administrador" + ex.getMessage()));
        }
        try {

            DaoItem daoItem = new DaoItem();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.item.setPregunta(this.pregunta);
            this.item.setImgItem(nombreImagen);
            this.item.setEstado(true);
            daoItem.registrar(session, item);
            
            // Se crea nodo pregunta en Red bayesiana
            CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
            DaoPregConc daoPregunta= new DaoPregConc();
            List<PregConc> lstPregConc=daoPregunta.verPorPregunta(session, this.pregunta.getIdPregunta());
            List<Concepto> lstConcepto= new ArrayList<>();
            for (int i = 0; i < lstPregConc.size(); i++) {
                lstConcepto.add(lstPregConc.get(i).getConcepto());
            }
            if(!rbDynamic.existPregunta(lstConcepto.get(0).getTema().getNombre(), this.pregunta.getNombrePreg())){
                rbDynamic.crearPregunta(lstConcepto.get(0).getTema().getNombre(), this.pregunta, lstConcepto, daoItem.verNumItemsPorPregunta(session, this.pregunta.getIdPregunta()));
            }else{
                rbDynamic.editarPregunta(lstConcepto.get(0).getTema().getNombre(), this.pregunta, lstConcepto, daoItem.verNumItemsPorPregunta(session, this.pregunta.getIdPregunta()));
            }
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "El registro fue realizado con éxito"));
            this.item = new Item();
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR:", "Contacte con el administrador" + ex.getMessage()));
            System.out.println("ERRORRRRRRRRRRRRRRRRRRR: "+ex.getMessage());
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public List<Item> getItemsPorPregunta(Pregunta pregunta) {
        this.session = null;
        this.transaction = null;
        this.pregunta=pregunta;
        try {
            DaoItem daoItem = new DaoItem();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            List<Item> t = daoItem.verPorPregunta(session, this.pregunta.getIdPregunta());
            transaction.commit();
            return t;
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            return null;
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public void abrirDialogoCrearItem(int codigo) {
        this.session=null;
        this.transaction=null;
        try {
            this.item = new Item();
            DaoPregunta daoItem = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.pregunta=daoItem.verPorCodigoPregunta(session, codigo);
            RequestContext.getCurrentInstance().update("frmCrearItems:panelCrearItems");
            RequestContext.getCurrentInstance().execute("PF('dialogCrearItems').show()");
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR ITEM CREAR:", "Contacte con el administrador" + ex.getMessage()));
        }
    }

    public void cargarPreguntaEditar(int codigo) {
        this.session = null;
        this.transaction = null;
        try {
            DaoItem daoItem = new DaoItem();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.item = daoItem.verPorCodigoItem(session, codigo);

            RequestContext.getCurrentInstance().update("frmEditarItem:panelEditarItem");
            RequestContext.getCurrentInstance().execute("PF('dialogEditarItem').show()");
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR ITEM EDITAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }
    
    public void cargarItemsPregunta(int codigo) {
        this.session = null;
        this.transaction = null;
        try {
            DaoPregunta daoItem = new DaoPregunta();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.pregunta=daoItem.verPorCodigoPregunta(session, codigo);
            RequestContext.getCurrentInstance().update("frmVerItems:panelVerItems");
            RequestContext.getCurrentInstance().execute("PF('dialogVerItems').show()");
            this.transaction.commit();
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR CARGAR ITEM EDITAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public void actualizar() {
        this.session = null;
        this.transaction = null;
        try {
            actualizarImg();
        } catch (IOException ex) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR DE LECTURA ESCRITURA:", "Contacte con el administrador" + ex.getMessage()));
        }
        
        try {
            DaoItem daoItem = new DaoItem();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            this.item.setImgItem(nombreImagen);
            daoItem.actualizar(this.session, this.item);
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Los cambios se realizaron con éxito."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR ACTUALIZAR:", "Contacte con el administrador" + ex.getMessage()));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public void eliminar() {
        this.session = null;
        this.transaction = null;
        try {
            DaoItem daoItem = new DaoItem();
            this.session = HibernateUtil.getSessionFactory().openSession();
            this.transaction = session.beginTransaction();
            daoItem.eliminar(this.session, this.item);
            // Se modifica la pregunta
            CrearBayesDynamic rbDynamic = new CrearBayesDynamic();
            DaoPregConc daoPregunta= new DaoPregConc();
            List<PregConc> lstPregConc=daoPregunta.verPorPregunta(session, this.pregunta.getIdPregunta());
            List<Concepto> lstConcepto= new ArrayList<>();
            for (int i = 0; i < lstPregConc.size(); i++) {
                lstConcepto.add(lstPregConc.get(i).getConcepto());
            }
            int numItems= daoItem.verNumItemsPorPregunta(session, this.pregunta.getIdPregunta()); 
            if(numItems==0){
                rbDynamic.eliminarPregunta(lstConcepto.get(0).getTema().getNombre(), pregunta);
            }else{
                rbDynamic.editarPregunta(lstConcepto.get(0).getTema().getNombre(), this.pregunta, lstConcepto,daoItem.verNumItemsPorPregunta(session, this.pregunta.getIdPregunta()));
            }
            this.transaction.commit();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Item eliminado correctamente."));
        } catch (Exception ex) {
            if (this.transaction != null) {
                this.transaction.rollback();
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL ELIMINAR:", "Contacte con el administrador, " + ex));
        } finally {
            if (this.session != null) {
                this.session.close();
            }
        }
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Pregunta getPregunta() {
        return pregunta;
    }

    public void setPregunta(Pregunta pregunta) {
        this.pregunta = pregunta;
    }

    public List<Item> getListaItem() {
        return listaItem;
    }

    public void setListaItem(List<Item> listaItem) {
        this.listaItem = listaItem;
    }

    public String getNombreImagen() {
        return nombreImagen;
    }

    public void setNombreImagen(String nombreImagen) {
        this.nombreImagen = nombreImagen;
    }

    public byte[] getContenidoImg() {
        return contenidoImg;
    }

    public void setContenidoImg(byte[] contenidoImg) {
        this.contenidoImg = contenidoImg;
    }

    public UploadedFile getImagen() {
        return imagen;
    }

    public void setImagen(UploadedFile imagen) {
        this.imagen = imagen;
    }
   
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        this.contenidoImg= file.getContents();
        this.nombreImagen=file.getFileName();
    }
    
    public void actualizarImg() throws IOException{
        InputStream inputS = null;
        OutputStream outputS= null;
        try{
            if(imagen.getSize()<=0){
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "ERROR:", "Debe seleccionar una imagen"));
                return;
            }
            
            inputS= this.imagen.getInputstream();
            ServletContext servletContex = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            String imgItems = (String) servletContex.getRealPath("/resources/imagen/imgItems") + "/" + imagen.getFileName();

            int contador = 1;
            File f = new File(imgItems);
            String nombre;
            String extension = ".png"; // en caso de no seleccionar una extensión
            int pos = imgItems.lastIndexOf(".");
            if (pos == -1) {
                nombre = imgItems;
            } else {
                nombre = imgItems.substring(0, pos);
                extension = imgItems.substring(pos);
            }

            boolean bandera = true;
            do {
                if (f.exists()) {
                    bandera = false;
                    imgItems = nombre +"_"+contador+extension;
                    contador++;
                    f = new File(imgItems);
                } else {
                    bandera = true;
                }
            } while (bandera == false);

            outputS = new FileOutputStream(f);

            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputS.read(bytes)) != -1) {
                outputS.write(bytes, 0, read);
            }
            this.nombreImagen = f.getName();            
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Correcto:", "Imagen de Item actualizado correctamente."));
        }catch(Exception ex){
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL, "ERROR AL GUARDAR IMAGEN:", "Contacte con el administrador, " + ex));
        }finally{
            if(inputS!=null){
                inputS.close();
            }
            if(outputS!=null){
                outputS.close();
            }
        }
    }
}
