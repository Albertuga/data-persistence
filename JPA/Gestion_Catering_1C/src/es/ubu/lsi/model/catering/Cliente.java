package es.ubu.lsi.model.catering;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

/**Cliente
 * Entity implementation class for Entity: Cliente
 * @author Alberto Rafael Muñoz Moreno
 */
@Entity
@Table(name="CLIENTE")
public class Cliente implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	private String cif;
	private String descripcion;
	//Llamo metodo embebido para tener dir, cp y ciudad
	@Embedded
	private DireccionPostal direccionPostal;
	
	//llamo al bono cliente FK
	@ManyToOne
	@JoinColumn(name="IDBONOCLIENTE")
	private BonoCliente bonoCliente;
	
	@OneToMany(mappedBy= "cliente") //bidireccional entre compras (Un cliente pertenece a varias compras) mapped propietario
	private Set<Compra> compras; //tengo que implementar compra
	
	//constructor vacio
	public Cliente() {
		super();
	}
	//autogenero getters, setters, hashcode, equals, y Tostring

	public String getCif() {
		return cif;
	}

	public void setCif(String cif) {
		this.cif = cif;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public DireccionPostal getDireccionPostal() {
		return direccionPostal;
	}

	public void setDireccionPostal(DireccionPostal direccionPostal) {
		this.direccionPostal = direccionPostal;
	}

	public BonoCliente getBonoCliente() {
		return bonoCliente;
	}

	public void setBonoCliente(BonoCliente bonoCliente) {
		this.bonoCliente = bonoCliente;
	}

	public Set<Compra> getCompras() {
		return compras;
	}

	public void setCompras(Set<Compra> compras) {
		this.compras = compras;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cif == null) ? 0 : cif.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Cliente)) {
			return false;
		}
		Cliente other = (Cliente) obj;
		if (cif == null) {
			if (other.cif != null) {
				return false;
			}
		} else if (!cif.equals(other.cif)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Cliente [cif=" + cif + ", descripcion=" + descripcion + ", direccionPostal=" + direccionPostal
				+ ", bonoCliente=" + bonoCliente + "]";
	}
	
	
   
}
