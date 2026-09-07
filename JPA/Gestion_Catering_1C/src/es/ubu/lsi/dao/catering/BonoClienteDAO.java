package es.ubu.lsi.dao.catering;

import java.util.List;
import javax.persistence.*;
import es.ubu.lsi.dao.JpaDAO;
import es.ubu.lsi.model.catering.BonoCliente;
/**BonoClienteDAO
 * Creo el DAO de bono cliente para implementarla en insertarCompra
 * durante el calculo del importe principalmente, a demas de informacion 
 * que debe recuperarse para descuentos.
 * 
 * */
public class BonoClienteDAO extends JpaDAO<BonoCliente, Integer>{

	public BonoClienteDAO(EntityManager em) {
		super(em);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<BonoCliente> findAll() {
		// TODO Auto-generated method stub
		//Enunciado pide que se implememtne
		return entityManager.createQuery("SELECT bc FROM BonoCliente bc", BonoCliente.class).getResultList();
	}
	

}
