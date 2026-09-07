package es.ubu.lsi.service.catering;

import java.util.Date;
import java.util.List;
import java.util.Set;

//Imports
import javax.persistence.EntityManager;

//DAOs y clases
import es.ubu.lsi.dao.catering.MenuDAO;
import es.ubu.lsi.dao.catering.CompraDAO;
import es.ubu.lsi.dao.catering.ClienteDAO;
import es.ubu.lsi.model.catering.Cliente;
import es.ubu.lsi.model.catering.Compra;
import es.ubu.lsi.model.catering.CompraPK;
import es.ubu.lsi.model.catering.Menu;
import es.ubu.lsi.service.PersistenceException;
//Persistence
import es.ubu.lsi.service.PersistenceService;

/**
 * ServiceImpl, clase que implementa la interface Service contiene los metodos
 * insertarCompra, quitarDescuento y consultarMenu
 * 
 * @author Alberto Rafael Muñoz Moreno
 */

public class ServiceImpl extends PersistenceService implements Service {

	/**
	 * insertarCompra Calcula la formula del importe. imp =(personas
	 * *precioMenu)-((personas *precioMenu)*descuento/100) comprueba existencias de
	 * clientes, menu y compra antes de ejecutar cualquier accion
	 */
	@Override
	public void insertarCompra(Date fecha, String cif, long idMenu, long personas) throws PersistenceException {
		// TODO Auto-generated method stub
		// instancio EntityManager
		EntityManager em = this.createSession();
		// LLamo instancias DAO
		ClienteDAO clienteDao = new ClienteDAO(em);
		MenuDAO menuDao = new MenuDAO(em);
		CompraDAO compraDao = new CompraDAO(em);

		// Comienza transaccion
		try {
			beginTransaction(em);
			// Hacemos comprobaciones varias
			if (fecha == null)
				throw new IncidentException(IncidentError.ERROR_IN_DATE);
			if (personas <= 0)
				throw new IncidentException(IncidentError.NEGATIVE_OR_ZERO_PEOPLE);
			// comprobamos si cliente existe
			Cliente cliente = clienteDao.findById(cif);
			if (cliente == null)
				throw new IncidentException(IncidentError.NOT_EXISTS_CLIENT);

			// comprobamos si menu existe
			Menu menu = menuDao.findById(idMenu);// cast para integer
			if (menu == null)
				throw new IncidentException(IncidentError.NOT_EXISTS_MENU);

			// comprobamos si compra Existe (clase compraPK para comprobar)
			CompraPK comp = new CompraPK();
			comp.setCif(cif);
			comp.setFecha(fecha); // agrego id y fecha de parametros y compruebo
			if (compraDao.findById(comp) != null)
				throw new IncidentException(IncidentError.EXISTS_PURCHASE); // compra existente!

			// obtenemos descuento a partir del cliente
			float descuentoCliente = 0;
			if (cliente.getBonoCliente() != null) { //verifico que tenga bono
				descuentoCliente = cliente.getBonoCliente().getDescuento();
			}

			// obtenemos precio a partir de menu
			float precioMenu = menu.getPrecio();

			// Calculos
			float impBase = personas * precioMenu;
			float importeTotal = impBase - (impBase * (descuentoCliente / 100));

			// comprobamos que sea un monto real antes de comeeter compra
			if (importeTotal <= 0)
				throw new IncidentException(IncidentError.NEGATIVE_OR_ZERO_IMPORT);

			// Creamos la compra
			Compra compraNueva = new Compra(); // creo compraNueva
			// Asigno cada parametro
			compraNueva.setId(comp);
			compraNueva.setPersonas(personas);
			compraNueva.setImporte(importeTotal);
			compraNueva.setCliente(cliente);
			compraNueva.setMenu(menu);
			// Guardo compra
			compraDao.persist(compraNueva);

			// commit
			commitTransaction(em);
		} catch (IncidentException e) {
			rollbackTransaction(em); // En caso de error rollback
			throw e; // relanza errores del TestClient
		} catch (Exception e) {
			rollbackTransaction(em);
			throw new PersistenceException("ERROR TECNICO PERSISTENCE: 	", e);
		} finally { // cerramos conexion.
			if (em != null && em.isOpen())
				em.close();
		}
	}

	/**
	 * quitarDescuento Para ello calculare el importe sin descuento y restare al
	 * importe anterior posteriormente lo reasignare al importe nuevo. Al cliente
	 * introducido se le deniega el descuento aplicado en todas sus compras,
	 * habiendo que actualizar dichas compras como si su descuento fuera 0. Se
	 * devuele el importe que se le ha añadido de más al aplicar el descuento.
	 */
	@Override
	public float quitarDescuento(String cif) throws PersistenceException {
		// TODO Auto-generated method stub
		// Instancio entityManager
		EntityManager em = this.createSession();

		float importeDescuento = 0.0f; // variable que contiene lo recuperado del descuento retirado

		try { // inicio transaccion
			beginTransaction(em);
			
			Cliente cliente = new ClienteDAO(em).findById(cif);
			if (cliente == null)
				throw new IncidentException(IncidentError.NOT_EXISTS_CLIENT);

			// obtengo compras de este cliente por set para evitar problemas con Hibernate
			// (no usar listas)
			Set<Compra> compras = cliente.getCompras();

			// por cada compra agrego a una variable que contiene todas, luego resto al
			// importe sin desc
			for (Compra c : compras) {
				float importeAnterior = c.getImporte();
				// nuevo importe
				float importeNuevo = c.getPersonas() * c.getMenu().getPrecio();

				c.setImporte(importeNuevo);
				importeDescuento += (importeNuevo - importeAnterior);
			}
			// Si todo va bien finalizo transaccion
			commitTransaction(em);
			return importeDescuento;
		} catch (Exception e) {
			rollbackTransaction(em); // En caso de que no vaya bien
			throw e;
		} finally {
			// cierro conexion
			if (em != null && em.isOpen())
				em.close();
		}
	}

	@Override
	public List<Menu> consultarMenu(long idMenu) throws PersistenceException {
		// TODO Auto-generated method stub
		// esto es una consulta solo de lectura por lo que no voy a abrir transaccion
		EntityManager em = this.createSession();
		MenuDAO menuDao = new MenuDAO(em);

		try {
			// Llamo al grafo
			List<Menu> listaMenu = menuDao.findAllWithGraph(idMenu);

			// compruebo que no este vacia
			if (listaMenu == null || listaMenu.isEmpty())
				throw new IncidentException(IncidentError.NOT_EXISTS_MENU);

			return listaMenu;
		} catch (IncidentException e) {
			throw e; // unicamente relanzo el error
		} catch (Exception e) {
			throw new PersistenceException("Error al consultar menu con grafos: ", e);
			} finally {//cierro conexiones 
				if (em != null && em.isOpen())
					em.close();
			}
	}

}
