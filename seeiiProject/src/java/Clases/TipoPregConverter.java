/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Clases;

import Pojo.Rol;
import Pojo.Tipopregunta;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author KathyR
 */
@FacesConverter(value = "tipoPregConverter")
public class TipoPregConverter implements Converter {

//    private List<Rol> listaRoles;
    public TipoPregConverter() {

    }

    @Override
    public Tipopregunta getAsObject(FacesContext context, UIComponent component, String value) {
        String tipo = value;
        Tipopregunta rol = new BeanRequest.BeanRTipoPregunta().consultarTipoPorNombre(tipo);
        return rol;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            if (value instanceof Pojo.Tipopregunta) {
                Tipopregunta m = (Tipopregunta) value;
                return m.getNombreTipo();
            }
            return "";
        }
        return "";
    }
}
