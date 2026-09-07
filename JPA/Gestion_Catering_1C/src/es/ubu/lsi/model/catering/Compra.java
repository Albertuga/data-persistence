package es.ubu.lsi.model.catering;

import java.io.Serializable;
import javax.persistence.*;

/**Compra
 * Entity implementation class for Entity: Compra
 *@author Alberto Rafael Muñoz Moreno
 */
@Entity
@Table(name="COMPRA")
public class Compra implements Serializable {

	private static final long serialVersionUID = 1L;

	//traigo mi pk de mi clase embebida
	@EmbeddedId
	private CompraPK id;
	//demas atributos que componen la compra
	@Column(name="PERSONAS")
	private long personas; //persona > 0
	@Column(name="IMPORTE")
	private float importe; //ver calculos en transaccion 
	
	@ManyToOne //muchas compras a un cliente
	@MapsId("cif") //traigo pk de cliente. (ejemplos de  P.25 - JPA RElaciones)
	@JoinColumn(name="CIF") //CIF es la relacion con cliente
	private Cliente cliente;
	
	//Relacion con menu = IDMENU
	@ManyToOne
	@JoinColumn(name="IDMENU")
	private Menu menu;
	
	//constructor vacio
	public Compra() {	}

	public CompraPK getId() {
		return id;
	}

	public void setId(CompraPK id) {
		this.id = id;
	}

	public long getPersonas() {
		return personas;
	}

	public void setPersonas(long personas) {
		this.personas = personas;
	}

	public float getImporte() {
		return importe;
	}

	public void setImporte(float importe) {
		this.importe = importe;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Compra)) {
			return false;
		}
		Compra other = (Compra) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Compra [id=" + id + ", personas=" + personas + ", importe=" + importe + "]";
	}
   
}
