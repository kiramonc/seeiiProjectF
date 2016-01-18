/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import BeanRequest.BeanRConcepto;
import Pojo.Concepto;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author KathyR
 */
@FacesConverter(value = "conceptoConverter")
public class ConceptoConverter implements Converter {

     public ConceptoConverter() {
    }

    @Override
    public Concepto getAsObject(FacesContext context, UIComponent component, String value) {
//        int tipo=Integer.parseInt(value);
        String tipo = value;
        Concepto concepto = new BeanRConcepto().consultarConceptoPorNombre(tipo);
        return concepto;
    }

  
    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            if (value instanceof Concepto) {
                
                Concepto c = (Concepto) value;
//                String cadena=Integer.toString(c.getIdConcepto());
//                System.out.println("EL OBJETO RECUPERADO ES CORRECTO Y EL ID ES:"+cadena);
                return c.getNombreConcepto();
            }
            System.out.println("NO ES UNA INSTANCIA DE Concepto");
            return "";
        }
        System.out.println("EL VALOR RECUPERADO ES NULO");
        return "";
    }
    
}
