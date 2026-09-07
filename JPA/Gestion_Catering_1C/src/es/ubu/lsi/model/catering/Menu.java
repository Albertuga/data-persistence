package es.ubu.lsi.model.catering;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.*;

/**
 * Menu Entity implementation class for Entity: Menu Creo relacion con compra,
 * uno a muchos. Implementamos el grafo para nuestro metodo findAllWithGraph() de
 * DAOS
 * 
 * @author Alberto Rafael Muñoz Moreno
 */

//consulta para findAllWithGraph MenuDAO
@Entity
@Table(name = "MENU")
@NamedEntityGraph(name = "Menu.completos", attributeNodes = {
		@NamedAttributeNode(value = "compras", subgraph = "compras.cliente") }, subgraphs = {
				//de compra a cliente
				@NamedSubgraph(name = "compras.cliente", attributeNodes = { @NamedAttributeNode(value = "cliente", subgraph = "cliente.bonoCliente") }),
				//de cliente a BonoCliente. esto evita cargas LAZY de bonocliente. evito N+1 problem
				@NamedSubgraph(name = "cliente.bonoCliente", attributeNodes = {@NamedAttributeNode("bonoCliente")})})
//Hacemos carga completa del grafo tomando en cuenta las tablas de Menu -> Compra -> Cliente -> BonoCliente
public class Menu implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private Long idMenu;
	private String descripcion;
	private float precio;

	// Relacion con compra un menu para varias compras
	@OneToMany(mappedBy = "menu")
	private Set<Compra> compras;

	// Constructor vacio
	public Menu() {
	}

	// Getters Seters
	public Long getIdMenu() {
		return idMenu;
	}

	public void setIdMenu(Long idMenu) {
		this.idMenu = idMenu;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
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
		result = prime * result + ((idMenu == null) ? 0 : idMenu.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Menu)) {
			return false;
		}
		Menu other = (Menu) obj;
		if (idMenu == null) {
			if (other.idMenu != null) {
				return false;
			}
		} else if (!idMenu.equals(other.idMenu)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Menu [idMenu=" + idMenu + ", descripcion=" + descripcion + ", precio=" + precio + "]";
	}

}
