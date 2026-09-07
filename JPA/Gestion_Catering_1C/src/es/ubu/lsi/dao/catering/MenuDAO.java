package es.ubu.lsi.dao.catering;

import java.util.List;
import javax.persistence.*;
import es.ubu.lsi.dao.JpaDAO;
import es.ubu.lsi.model.catering.Menu;


public class MenuDAO extends JpaDAO <Menu, Long> {
	//retorno constructor con EntityManager inicializado
	public MenuDAO(EntityManager em) {	
		super(em);
	}
	@Override
	public List<Menu> findAll() {
		// TODO Auto-generated method stub
		//consulta que extrae todo de menu
		return entityManager.createQuery("SELECT m FROM Menu m", Menu.class).getResultList();
	}
	
	/*Recuperamos los menu con compras y clientes con grafos
	 * usado por consultarMenu
	 * definido en @NamedEntityGraph clase Menu
	 * Ejemplo tomado del codigo 12 p9
	 * */
	public List<Menu> findAllWithGraph(long idMenu) {
		EntityGraph<?> grafico = entityManager.getEntityGraph("Menu.completos"); //Este es el nombre que he definido en la clase menu
		return entityManager.createQuery("SELECT m FROM Menu m WHERE m.idMenu = :id", Menu.class).setParameter("id", idMenu)
				.setHint("javax.persistence.loadgraph", grafico) //loadgraph para que sea EAGER
				.getResultList(); //obtengo resultado en lista de valores P11
	}
}
