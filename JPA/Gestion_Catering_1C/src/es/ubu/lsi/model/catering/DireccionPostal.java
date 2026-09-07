package es.ubu.lsi.model.catering;

import java.io.Serializable;
import javax.persistence.Embeddable; //para esta clase embebida solo usare esta lib
import javax.persistence.Column;

/**
 * DireccionPostal
 * Clase que contiene la direccion, cp y ciudad que complementa la clase cliente 
 * Solicitado en p2 del enunciado.
 * "Dado que existen atributos relacionados entre sí (i.e., direccion, cp, ciudad) 
 * se pide implementar esto con un tipo embebido adicional, de nombre DireccionPostal 
 * y que puede ser utilizada en Cliente."
 * @author Alberto Rafael Muñoz Moreno
 * 
 * */
@Embeddable
public class DireccionPostal implements Serializable {
	//mantengo para eliminar warning
	private static final long serialVersionUID = 1L;

	//implemento los atributos de las tablas 
	private String direccion;
	
	@Column(name="CP")
	private String CodigoPostal;
	
	private String ciudad;
	
	//Constructor vacio
	public DireccionPostal() {}

	//Getters and Setters ( clic >> source) 
	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getCodigoPostal() {
		return CodigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		CodigoPostal = codigoPostal;
	}

	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}
//implemento hashcode y equals
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((CodigoPostal == null) ? 0 : CodigoPostal.hashCode());
		result = prime * result + ((ciudad == null) ? 0 : ciudad.hashCode());
		result = prime * result + ((direccion == null) ? 0 : direccion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DireccionPostal)) {
			return false;
		}
		DireccionPostal other = (DireccionPostal) obj;
		if (CodigoPostal == null) {
			if (other.CodigoPostal != null) {
				return false;
			}
		} else if (!CodigoPostal.equals(other.CodigoPostal)) {
			return false;
		}
		if (ciudad == null) {
			if (other.ciudad != null) {
				return false;
			}
		} else if (!ciudad.equals(other.ciudad)) {
			return false;
		}
		if (direccion == null) {
			if (other.direccion != null) {
				return false;
			}
		} else if (!direccion.equals(other.direccion)) {
			return false;
		}
		return true;
	}
 //ToString
	@Override
	public String toString() {
		return "DireccionPostal [direccion=" + direccion + ", CodigoPostal=" + CodigoPostal + ", ciudad=" + ciudad
				+ "]";
	}
	
	
	
}
