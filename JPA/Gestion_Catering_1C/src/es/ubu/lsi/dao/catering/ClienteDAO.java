package es.ubu.lsi.dao.catering;

import java.util.List;
import javax.persistence.*;
import es.ubu.lsi.dao.JpaDAO;
import es.ubu.lsi.model.catering.Cliente;

public class ClienteDAO extends JpaDAO<Cliente, String >{

	public ClienteDAO(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Cliente> findAll() {
		// TODO Auto-generated method stub
		//retornamos todos los clientes
		return entityManager.createQuery("SELECT cli FROM Cliente cli", Cliente.class).getResultList();
	}
}
