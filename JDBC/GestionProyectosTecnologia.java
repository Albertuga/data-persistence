package lsi.ubu.solucion;
/*
 * Las transacciones deben implementarse en lsi.ubu.solucion.GestionProyectoTecnologia.java
 * por lo que usamos la plantilla de esqueleto.
 * */

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement; 	//Para las consultas
import java.sql.ResultSet;			//para representar tablas y manipular

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lsi.ubu.enunciado.GestionProyectosTecnologiaException; //Importamos las excepciones de los datos
import lsi.ubu.util.ExecuteScript;
import lsi.ubu.util.PoolDeConexiones;


/**
 * GestionProyectosTenologia:
 * Implementa la gestion de proyectos de tecnolgia
 * 
 * @author <a href="mailto:amm1084@alu.ubu.es">Alberto Rafael Munoz Moreno</a>
 * @version 1.1
 * @since 1.0 
 */
public class GestionProyectosTecnologia {
	
	private static Logger logger = LoggerFactory.getLogger(GestionProyectosTecnologia.class);

	private static final String script_path = "sql/";

	public static void main(String[] args) throws SQLException{		
		tests();

		System.out.println("FIN.............");
	}
	

	/** insertarPerfil 
	 * Añadirá una entrada en la tabla perfiles_asignados con la información contenida por parámetros 
	 * (numero = numPersonas) y calculando el campo importe como importe = numPersonas*horas*tarifa (tabla perfil).
	 * Se debe actualizar la tabla proyecto sumando las horas y el importe correspondiente al proyecto en concreto.
	 * **/
	public static void insertarPerfil(int idPerfil, int idProyecto, int numPersonas,int horas) throws SQLException {
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;

		//empleo PreparedStatment para consultas (ejemplos tomados de p.23 y 32
		PreparedStatement pstPerfil = null; 	//pst que contiene el perfil 
		PreparedStatement pstProyecto = null;	//pst que contiene el proyecto
		PreparedStatement inserta = null;		//usado en insert
		PreparedStatement actualiza = null;		//usado para updates
		
		ResultSet perfilRs = null; 				//Inicializo rs que contendra el valor de perfil existente (o no)
		ResultSet proyectoRs= null;

	
		try{
			con = pool.getConnection();
			//Salida para confirmar creacion de la conexion (debug y ejemplos)
			//System.out.println("Conexion Creada.");
			
			//Inseramos alguas validaciones para el calculo de importe
			//Validacion de horas
			if (horas <= 0) {
				throw new GestionProyectosTecnologiaException(2); //numero horas incorrecto
			}
			//validacion de numpersonas
			if (numPersonas <=0 ) {
				throw new GestionProyectosTecnologiaException(5); //numero de personas incorrecto
			}
			
			// Comprobamos que el perfil exista y obtenemos la tarifa 
			pstPerfil = con.prepareStatement("SELECT tarifa FROM perfil WHERE id_perfil = ?");
			pstPerfil.setInt(1, idPerfil);		
			perfilRs = pstPerfil.executeQuery();
			
			//En caso de que no encontremos el perfil salta exception 3
			if (!perfilRs.next()) {
				throw new GestionProyectosTecnologiaException(3); //Perfil inexistente
			}
			
			//obtenemos tarifa del perfil, como es numeric = BigDecimal (P.17)
			BigDecimal tarifa = perfilRs.getBigDecimal("tarifa");
			
			// Comprobamos ahora el proyecto existente en caso contrario exception 4
			//para ello empleamos la preparedStatement creado y el rs
			pstProyecto = con.prepareStatement("SELECT id_proyecto FROM proyecto where id_proyecto = ?");
			pstProyecto.setInt(1, idProyecto); 		
			proyectoRs = pstProyecto.executeQuery();
			
			//Comprobacion
			if(!proyectoRs.next()) {
				throw new GestionProyectosTecnologiaException(4); //Proyecto inexistente
			}
			
			//Calculo de importes
			//Tenemos numero de personas, tarifa y horas importe = numPersonas*horas*tarifa
			BigDecimal importe = tarifa.multiply(new BigDecimal(numPersonas)).multiply(new BigDecimal(horas));
			
			//vemos si el importe es negativo
			if(importe.compareTo(BigDecimal.ZERO)<0) {
				throw new GestionProyectosTecnologiaException(8);  //Importe incorrecto
			}
			
			//Insertamos perfiles_asignados
			inserta = con.prepareStatement("insert into perfiles_asignados(id_perfil, id_proyecto, numero, horas, importe) " + "values (?, ?, ?, ?, ?)");
			inserta.setInt(1, idPerfil);
			inserta.setInt(2, idProyecto);
			inserta.setInt(3, numPersonas);
			inserta.setInt(4, horas);
			inserta.setBigDecimal(5, importe);
			
			//Probamos insertando, si hay errores
			try {
				inserta.executeUpdate();
			}catch (SQLException e){ //Extraigo excepcion, en caso que el perfil sea duplicado o ya este asignado = 1
				if (e.getErrorCode() == 1) {
					throw new GestionProyectosTecnologiaException(1); //"Perfil ya asignado a proyecto"
				}else {		//Si no es 
					throw e; //pongo cualquier error que salga si no es esta excepcion
				}
			}
			
			// UPDATES del proyecto: sumamos las horas e importes
			actualiza = con.prepareStatement("Update proyecto SET horas= horas + ?, importe = importe + ? " + "WHERE id_proyecto = ?");
			actualiza.setInt(1, horas);
			actualiza.setBigDecimal(2, importe);
			actualiza.setInt(3, idProyecto);
			actualiza.executeUpdate(); //importante UPDATE!
			
			con.commit(); //En caso de que todo vaya bien confirmo transaccion 
			//en caso contrario
		} catch (SQLException e) {
			//Completar por el alumno.hago rollback imprimo error. Algo Falla 
			con.rollback(); //algo falla , hago rollback
			if(!(e instanceof GestionProyectosTecnologiaException)) { //en caso que no sean mensajes de la excepcion envio a logger
				logger.error(e.getMessage());
			}
			
			throw e; 

		} finally {
			/*A rellenar por el alumno, liberar recursos*/
		try {//finalizamos las conexiones //Simiplar al PruebaProyectoSimple.java
			if (perfilRs != null) perfilRs.close();			//cierro resultSet
			if (proyectoRs != null) proyectoRs.close();		//cierro resultSet
			if (pstPerfil != null) pstPerfil.close();		//Cierro los PreparedStatement
			if (pstProyecto != null) pstProyecto.close();
			if (inserta != null) inserta.close();
			if (actualiza != null) actualiza.close();
			if (con != null) con.close();
			//System.out.println("Conexion cerrada."); //debug
			
		} catch (SQLException e){
			System.out.println("Error cerrando la conexion.");
			System.out.println(e.getMessage());
		}		
		}
	}
	/**eliminarTipoProyecto
	 * Borra de la tabla tipo_proyecto el registro con el id pasado por parámetro así como los registros asociados 
	 * en las demás tablas. 
	 * Devuelve 0 si no existe el id introducido y 1 en caso de que exista y se haya borrado bien.
	 * **/
	public static int eliminarTipoProyecto(int idTipoProyecto) 	throws SQLException {
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;
		
		//Instancio PS para consultas
		PreparedStatement eliminaPerf = null, eliminaProy=null, eliminaTipo = null, compruebaProyecto = null;
		ResultSet compProy = null;
	
		try{
			con = pool.getConnection();
				 //1. vemos si el proyecto existe
			compruebaProyecto = con.prepareStatement("SELECT id_tipo_proyecto from tipo_proyecto where id_tipo_proyecto = ?");
			compruebaProyecto.setInt(1,  idTipoProyecto);
			compProy = compruebaProyecto.executeQuery();
			if (!compProy.next()) { //omito rs ejecutando query
				throw new GestionProyectosTecnologiaException(6); //tipo proyecto inexistente

			} 
			//El enunciado dice que si no tengo siguiente proyecto retorno 0 pero priorizo excepcion por los teses
			/*if (!compProy.next()) { //si no existe retorno 0
				con.rollback(); //no necesario porque no han habido cambios en teoria pero pongo para finalizar trans
				return 0;
			} */

			
			//IMPORTANTE. no usar cascade si no error!
			//2. borrado de perfiles asignados a  proyectos del mismo tipo. ("asi como los registros asociados en las demas tablas")
			//subconsulta seleccionando "ese proyecto" extrayendo perfil asignado del id_proyecto seleccionado
			eliminaPerf = con.prepareStatement("DELETE perfiles_asignados where id_proyecto in "
					+ "(SELECT id_proyecto from proyecto where id_tipo_proyecto = ?)"); //misma select que antes que busca proyecto "?"
			eliminaPerf.setInt(1, idTipoProyecto); //paso parametro
			eliminaPerf.executeUpdate();
			
			//3. borrar proyectos del mismo tipo ("asi como los registros asociados en las demas tablas")
			eliminaProy = con.prepareStatement("DELETE from proyecto WHERE id_tipo_proyecto = ?");
			eliminaProy.setInt(1, idTipoProyecto);
			eliminaProy.executeUpdate();
			
			//4. "borrar de la tabla tipo_proyecto el registro con el id pasado por parametro"
			eliminaTipo = con.prepareStatement("DELETE from tipo_proyecto WHERE id_tipo_proyecto = ?");
			eliminaTipo.setInt(1, idTipoProyecto);
			eliminaTipo.executeUpdate();
			
			//si todo va bien commit y return 1
			con.commit();
			return 1;
			//si no
		} catch (SQLException e) {
			//Completar por el alumno			
			con.rollback(); //va mal algo
			if(!(e instanceof GestionProyectosTecnologiaException)) {
				logger.error(e.getMessage());
			}
			throw e;		

		} finally { //implemento igual al tutorial PruebaProyectoSimple.java
			/*A rellenar por el alumno, liberar recursos*/
			try {
				if (eliminaPerf != null) eliminaPerf.close(); // Cierro PS
				if (eliminaProy != null) eliminaProy.close();
				if (eliminaTipo != null) eliminaTipo.close();
				if (compruebaProyecto != null) compruebaProyecto.close();
				if (compProy != null) compProy.close();			//Cierro RS
				if (con != null) con.close();
				
			} catch (SQLException e) {
				logger.error("Error al cierre de la conexion; " + e.getMessage());
				
			}
		}
	}
	
	/**consultaCliente
	 * Mostrará por salida estándar un listado ordenado por id_proyecto de todos los datos del modelo 
	 * indicado relacionados con el identificador de cliente pasado por parámetro. 
	 * Cada combinación de tuplas llevarán los campos concatenados con el carácter guion medio “-“.**/
	public static void consultaCliente(int idCliente) throws SQLException {

				
		PoolDeConexiones pool = PoolDeConexiones.getInstance();
		Connection con=null;
		
		//PS para hacer las consultas
		PreparedStatement verificaCliente = null;
		PreparedStatement consulta = null; //esta consulta hace joins para agregar al info del cliente
		ResultSet verCliente =  null, rsConsulta = null;
	
		try{
			con = pool.getConnection();
			
			//1. vemos si el cliente existe
			verificaCliente = con.prepareStatement("SELECT id_cliente from cliente where id_cliente = ?");
			verificaCliente.setInt(1, idCliente);
			verCliente = verificaCliente.executeQuery();
			
			if (!verCliente.next()) { //En caso que no exista.
				throw new GestionProyectosTecnologiaException(7); //Cliente inexistente
			}
			
			//consulta que  muestra "Todos los datos del modelo" ordenados por id_proyecto por cada cliente.
			consulta = con.prepareStatement("Select c.id_cliente, c.nombre_cliente, c.nombre_sector_cliente, "
					+ "p.id_proyecto, p.nombre_proyecto, p.fecha_inicio, p.fecha_fin, p.horas, p.importe, "
					+ "tip.id_tipo_proyecto, tip.nombre AS nombre_tipo_proyecto, "
					+ "pasig.id_perfil, pasig.numero, pasig.horas AS horas_perfil_asig, pasig.importe AS importe_perfil_asig, "
					+ "pf.nombre_perfil, pf.tarifa "
					+ "FROM cliente c "
					+ "JOIN proyecto p ON c.id_cliente=p.id_cliente JOIN tipo_proyecto tip ON p.id_tipo_proyecto = tip.id_tipo_proyecto "
					+ "JOIN perfiles_asignados pasig ON p.id_proyecto = pasig.id_proyecto "
					+ "JOIN perfil pf ON pasig.id_perfil = pf.id_perfil "
					+ "where c.id_cliente = ? "
					+ "order by p.id_proyecto");
			consulta.setInt(1, idCliente);
			rsConsulta = consulta.executeQuery();
			
			//mostramos los resultados con un guion " - " separador........
			//mostramos cada uno de los datos solicitados en la select de la consulta anterior.
			while (rsConsulta.next()) {
				System.out.println(rsConsulta.getString("id_cliente") + " - " + rsConsulta.getString("nombre_cliente")
				 + " - " + rsConsulta.getString("nombre_sector_cliente") + " - " + rsConsulta.getString("id_proyecto")
				 + " - " + rsConsulta.getString("nombre_proyecto") 
				 + " - " + rsConsulta.getString("fecha_inicio") + " - " + rsConsulta.getString("fecha_fin")
				 + " - " + rsConsulta.getString("horas") + " - " + rsConsulta.getString("importe")
				 + " - " + rsConsulta.getString("id_tipo_proyecto") + " - " + rsConsulta.getString("nombre_tipo_proyecto")
				 + " - " + rsConsulta.getString("id_perfil") + " - " + rsConsulta.getString("numero")
				 + " - " + rsConsulta.getString("horas_perfil_asig") + " - " + rsConsulta.getString("importe_perfil_asig")
				 + " - " + rsConsulta.getString("nombre_perfil") + " - " + rsConsulta.getString("tarifa"));
			}
			//si todo va correcto
			con.commit();
			
		} catch (SQLException e) {
			//Completar por el alumno			
			//si no va correcto rollback
			con.rollback();
			if(!(e instanceof GestionProyectosTecnologiaException)) {
				logger.error(e.getMessage());
			}
			throw e;		

		} finally {
			/*A rellenar por el alumno, liberar recursos*/
			//implemento cierre de conexiones como en ejemplo pruebaproyecto simple
			try {
				if (verificaCliente != null) verificaCliente.close();
				if (verCliente != null) verCliente.close();
				if (consulta !=null) consulta.close();
				if (rsConsulta != null) rsConsulta.close();
				if (con != null) con.close();
			}catch (SQLException e) {
				logger.error("Error al cierre de la conexion; " + e.getMessage());
			}
		}		
	}
	
	static public void creaTablas() {
		ExecuteScript.run(script_path + "gestion_proyectos_tecnologia.sql");
	}
	
	//Metodo que reinicia la bd y lanzará todas las pruebas.
	static void tests() throws SQLException{
		creaTablas();
		
		PoolDeConexiones pool = PoolDeConexiones.getInstance();		
		
		//Relatar caso por caso utilizando el siguiente procedure para inicializar los datos
		
		CallableStatement cll_reinicia=null;
		Connection conn = null;
		
		try {
			//Reinicio filas
			conn = pool.getConnection();
			cll_reinicia = conn.prepareCall("{call inicializa_test}");
			cll_reinicia.execute();
		} catch (SQLException e) {				
			logger.error(e.getMessage());			
		} finally {
			if (cll_reinicia!=null) cll_reinicia.close();
			if (conn!=null) conn.close();
		
		}	
		
		/*
		 * PRUEBAS (TESES)
		 * */
		///// insertarPerfil ////
		try {
			insertarPerfil(4,1,2,3);
			System.out.println("Insercion correcta");
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//perfil asignado al mismo proyecto
		try {
			insertarPerfil(4,1,2,3); //mismos datos ya existentes
			System.out.println("ERROR, deberia lanzarse excepcion 1.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//Inserto horas = 0
		try {
			insertarPerfil(4,1,2,0); 
			System.out.println("ERROR, deberia lanzarse excepcion 2.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//Inserto horas negativas
		try {
			insertarPerfil(4,1,2,-5); 
			System.out.println("ERROR, deberia lanzarse excepcion 2.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//Perfiles inexistentes
		try {
			insertarPerfil(1313342,1,2,3); 
			System.out.println("ERROR, deberia lanzarse excepcion 3.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//proyecto inexistente
		try {
			insertarPerfil(4,131,2,3); 
			System.out.println("ERROR, deberia lanzarse excepcion 4.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//0 personas
		try {
			insertarPerfil(4,3,0,3); 
			System.out.println("ERROR, deberia lanzarse excepcion 5.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		//numero de personas negativo
		try {
			insertarPerfil(4,3,-9,3); 
			System.out.println("ERROR, deberia lanzarse excepcion 5.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}
		
		////// eliminarTipoProyecto ///////
		//un tipo que existe y tiene proyectos y perfiles asociados (normal)
		try {
			int resultado = eliminarTipoProyecto(1);
			System.out.println("CORRECTO - resultado: " + resultado); //deberia imprimir 1
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		} 
		
		//Intento eliminar tipo proyecto inexistente
		try {
	        eliminarTipoProyecto(9999); // proyecto inexistente codigo 6
	        System.out.println("ERROR, deberia lanzarse excepcion 6");
	    } catch (GestionProyectosTecnologiaException e) {
	        // Verificamos que el código de error sea el 6 según el enunciado 
	        if (e.getErrorCode() == 6) {
	            System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: " + e.getErrorCode());
	        } else {
	            System.out.println("ERROR: "+ e.getErrorCode());
	        }
	    } catch (SQLException e) {
	        System.out.println("ERROR: " + e.getMessage());
	    }
		////// consultaCliente ///////		
		//cliente que existe y tiene proyectos (normal)
		try {
			System.out.println("*_*_*_*Proyectos del Cliente 1*_*_*_*_*");
			consultaCliente(1); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		} //nos debe salir todos los proyectos que tiene asociados el cliente 1

		
		//Consulta de cliente inexistente (prueba 7)
		try {
			consultaCliente(987);
			System.out.println("ERROR, deberia lanzarse excepcion 7.");
		}catch (GestionProyectosTecnologiaException e) {
			System.out.println("CORRECTO: " + e.getMessage() + ". Excepcion nº: "+ e.getErrorCode()); 
		}catch (SQLException e) {
			System.out.println("ERROR: " + e.getMessage());
		}		
	}
}