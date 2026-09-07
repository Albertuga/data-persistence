package es.ubu.lsi.dao.catering;

import java.util.List;
import javax.persistence.*;
import es.ubu.lsi.dao.JpaDAO;
import es.ubu.lsi.model.catering.Compra;
import es.ubu.lsi.model.catering.CompraPK; //pk esta en una embebida

public class CompraDAO extends JpaDAO<Compra, CompraPK>{

	public CompraDAO(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Compra> findAll() {
		// TODO Auto-generated method stub
		//retorno todas las compras
		return entityManager.createQuery("SELECT com FROM Compra com", Compra.class).getResultList();
	}

}
