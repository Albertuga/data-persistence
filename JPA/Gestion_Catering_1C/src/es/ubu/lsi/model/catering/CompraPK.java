package es.ubu.lsi.model.catering;

import java.io.Serializable;
import java.util.*;
import javax.persistence.*;	

/**CompraPK
 * Como la tabla compra tiene una clave primaria compuesta, creo un @Embeddable para la pk
 * 
 * @author Alberto Rafael Muñoz Moreno
 * 
 * */
@Embeddable
public class CompraPK implements Serializable {
	//Mantengo para eliminar warning
	private static final long serialVersionUID = 1L;

	//Atributos que componen pk
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="FECHA")
	private Date fecha;
	@Column(name="CIF")
	private String cif;
	//constructor vacio
	public CompraPK() {}
		
	//Getters setters, HashCode, equals, ToString autogenerados.
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getCif() {
		return cif;
	}
	public void setCif(String cif) {
		this.cif = cif;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cif == null) ? 0 : cif.hashCode());
		result = prime * result + ((fecha == null) ? 0 : fecha.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof CompraPK)) {
			return false;
		}
		CompraPK other = (CompraPK) obj;
		if (cif == null) {
			if (other.cif != null) {
				return false;
			}
		} else if (!cif.equals(other.cif)) {
			return false;
		}
		if (fecha == null) {
			if (other.fecha != null) {
				return false;
			}
		} else if (!fecha.equals(other.fecha)) {
			return false;
		}
		return true;
	}
	@Override
	public String toString() {
		return "CompraId [fecha=" + fecha + ", cif=" + cif + "]";
	}
	
}
