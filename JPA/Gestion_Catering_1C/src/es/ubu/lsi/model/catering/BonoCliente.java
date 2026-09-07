package es.ubu.lsi.model.catering;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

/** BonoCliente
 * Entity implementation class for Entity: BonoCliente
 * @author Alberto Rafael Muñoz Moreno
 */
@Entity
@Table(name="BONOCLIENTE")
public class BonoCliente implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="IDBONOCLIENTE")
	private int idBonoCliente;
	private String bono;
	private float descuento;
	
	//Relacion bidireccional
	@OneToMany(mappedBy = "bonoCliente")
	private Set<Cliente> clientes; //import class cliente tengo que implementar
	
	public BonoCliente() {}

	public int getIdBonoCliente() {
		return idBonoCliente;
	}

	public void setIdBonoCliente(int idBonoCliente) {
		this.idBonoCliente = idBonoCliente;
	}

	public String getBono() {
		return bono;
	}

	public void setBono(String bono) {
		this.bono = bono;
	}

	public float getDescuento() {
		return descuento;
	}

	public void setDescuento(float descuento) {
		this.descuento = descuento;
	}

	public Set<Cliente> getClientes() {
		return clientes;
	}

	public void setClientes(Set<Cliente> clientes) {
		this.clientes = clientes;
	}
	//HAshcode and equals implementando unicamente bonocliente
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + idBonoCliente;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BonoCliente)) {
			return false;
		}
		BonoCliente other = (BonoCliente) obj;
		if (idBonoCliente != other.idBonoCliente) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "BonoCliente [idBonoCliente=" + idBonoCliente + "]";
	}
	
}
