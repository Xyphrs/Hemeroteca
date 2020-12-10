package com.company;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.interfaces.RSAKey;
import java.sql.Connection;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.io.InputStream;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DBAccessor {
	private String dbname;
	private String host;
	private String port;
	private String user;
	private String passwd;
	private String schema;
	Connection conn = null;

	/**
	 * Initializes the class loading the database properties file and assigns
	 * values to the instance variables.
	 * 
	 * @throws RuntimeException
	 *             Properties file could not be found.
	 */
	public void init() {
		Properties prop = new Properties();
		InputStream propStream = this.getClass().getClassLoader().getResourceAsStream("db.properties");

		try {
			prop.load(propStream);
			this.host = prop.getProperty("host");
			this.port = prop.getProperty("port");
			this.dbname = prop.getProperty("dbname");
			this.schema = prop.getProperty("schema");
		} catch (IOException e) {
			String message = "ERROR: db.properties file could not be found";
			System.err.println(message);
			throw new RuntimeException(message, e);
		}
	}

	/**
	 * Obtains a {@link Connection} to the database, based on the values of the
	 * <code>db.properties</code> file.
	 * 
	 * @return DB connection or null if a problem occurred when trying to
	 *         connect.
	 */
	public Connection getConnection(Identity identity) {

		// Implement the DB connection
		String url = null;
		try {
			// Loads the driver
			Class.forName("org.postgresql.Driver");

			// Preprara connexió a la base de dades
			StringBuffer sbUrl = new StringBuffer();
			sbUrl.append("jdbc:postgresql:");
			if (host != null && !host.equals("")) {
				sbUrl.append("//").append(host);
				if (port != null && !port.equals("")) {
					sbUrl.append(":").append(port);
				}
			}
			sbUrl.append("/").append(dbname);
			url = sbUrl.toString();

			// Utilitza connexió a la base de dades
			conn = DriverManager.getConnection(url, identity.getUser(), identity.getPassword());
			conn.setAutoCommit(true);
		} catch (ClassNotFoundException e1) {
			System.err.println("ERROR: Al Carregar el driver JDBC");
			System.err.println(e1.getMessage());
		} catch (SQLException e2) {
			System.err.println("ERROR: No connectat  a la BD " + url);
			System.err.println(e2.getMessage());
		}

		// Sets the search_path
		if (conn != null) {
			Statement statement = null;
			try {
				statement = conn.createStatement();
				statement.executeUpdate("SET search_path TO " + this.schema);
				// missatge de prova: verificació
				System.out.println("OK: connectat a l'esquema " + this.schema + " de la base de dades " + url
						+ " usuari: " + user + " password:" + passwd);
				System.out.println();
				//
			} catch (SQLException e) {
				System.err.println("ERROR: Unable to set search_path");
				System.err.println(e.getMessage());
			} finally {
				try {
					statement.close();
				} catch (SQLException e) {
					System.err.println("ERROR: Closing statement");
					System.err.println(e.getMessage());
				}
			}
		}

		return conn;
	}

	public void altaAutor() throws SQLException, IOException {
		Scanner reader = new Scanner(System.in);
		System.out.println("Introdueix el id de l'autor");
		int id = reader.nextInt();
		System.out.println("Introdueix el nom");
		reader.nextLine();
		String nom = reader.nextLine();
		System.out.println("Introdueix l'any de naixement");
		String any_naixement = reader.nextLine();
		System.out.println("Introdueix la nacionalitat");
		String nacionalitat = reader.nextLine();
		System.out.println("Es actiu? (S/N)");
		String actiu = reader.nextLine();
		
		Statement statement = null;
		statement = conn.createStatement();
		statement.executeUpdate("INSERT INTO autors VALUES ("+id+",'"+nom+"','"+any_naixement+"','"+nacionalitat+"','"+actiu+"')");


	}

	public void altaRevista() throws SQLException, NumberFormatException, IOException, ParseException {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Scanner reader = new Scanner(System.in);
		System.out.println("Introdueix el id de la revista");
		int id = reader.nextInt();
		System.out.println("Introdueix el titol");
		reader.nextLine();
		String titol = reader.nextLine();
		System.out.println("Introdueix la data de publicacio (yyyy-mm-dd)");
		Date date = format.parse(reader.nextLine());
		
		Statement statement = null;
		statement = conn.createStatement();
		statement.executeUpdate("INSERT INTO revistes (id_revista, titol, data_publicacio) VALUES ("+id+",'"+titol+"','"+date+"')");

	}

	public void altaArticle() throws SQLException, NumberFormatException, IOException, ParseException {

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Scanner scanner = new Scanner(System.in);
		System.out.println("ID Article");
		int id = scanner.nextInt();
		System.out.println("ID Autor");
		int idautor = scanner.nextInt();
		System.out.println("Titol");
		scanner.nextLine();
		String titol = scanner.nextLine();
		System.out.println("Fecha (YYYY-MM-DD)");
		Date fecha = simpleDateFormat.parse(scanner.nextLine());
		System.out.println("Publicable? (S/N)");
		String publicable = scanner.nextLine();

		Statement statement = null;
		statement = conn.createStatement();
		statement.executeUpdate("INSERT INTO articles (id_article, id_autor, titol, data_creacio, publicable ) VALUES ("+id+", "+idautor+", '"+titol+"', '"+fecha+"', '"+publicable+"')");
	}
	
	public void afegeixArticleARevista(Connection conn) throws SQLException {

		ResultSet rs;
		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		try {
			rs = st.executeQuery("SELECT * FROM articles WHERE id_revista IS NULL");

			if (rs.getFetchSize() == 0) {
				System.out.println("No hi ha articles pendents d'associar revistes. ");
			} else {
				while (rs.next()) {
					System.out.println("Titol: " + rs.getString("titol"));
					
					System.out.println("Vol incorporar aquest article a una revista?");
					String resposta = br.readLine();

					if (resposta.equals("si")) {
						// demana l'identificador de la revista
						System.out.println("Introdueix el id de la revista");
						int idRevista = Integer.parseInt(br.readLine());
						// actualitza el camp
						rs.updateInt("id_revista", idRevista);
						// actualitza la fila
						rs.updateRow();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void actualitzarTitolRevistes(Connection conn) throws SQLException, IOException {

		// Estoy bastante seguro de que esto esta bien pero no tengo ni idea porque no.
		// Pasa lo mismo que en el de arriba (afegeixArticleARevista) que ya estaba hecho de antes y no lo he tocado.

		Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = st.executeQuery("SELECT * FROM revistes;");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);

		if (rs.getFetchSize() == 0) {
			System.out.println("No hay revistes.");
		} else {
			while (rs.next()) {
				System.out.println("Titol: " + rs.getString("titol"));
				System.out.println("Quieres cambiar el titulo de esta revista?(S/N)");
				String resposta = br.readLine();

				if (resposta.equals("S"))
					System.out.println("Introdueix el titol de la revista");
					String titolRevista = br.readLine();
					rs.updateString("titol", titolRevista);
					rs.updateRow();
			}
		}
	}

	public void desassignaArticleARevista(Connection conn) throws SQLException {

		Scanner scanner = new Scanner(System.in);
		System.out.println("Introduce ID de una revista");
		int revistaid = scanner.nextInt();
		Statement statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = statement.executeQuery(" SELECT * FROM articles WHERE id_revista = " + revistaid + ";");

		while (rs.next()) {
			System.out.println("ID: " + rs.getString(1) + "\tTitulo: " + rs.getString(3));

			System.out.println("Eliminar ID de revista de este articulo? (Y/N)");
			String eliminararticulo = scanner.next();
			if (eliminararticulo.equals("y")) {

				rs.updateNull("id_revista");
				rs.updateRow();
			} else {
				System.out.println("Cancelando dessignacion de Articulo a Revista");
			}
		}
	}
	
	public void mostraAutors() throws SQLException, IOException {
		Statement st = conn.createStatement();
		Scanner reader = new Scanner(System.in);
		ResultSet rs;

		rs = st.executeQuery("SELECT * FROM autors");
		while (rs.next()) System.out.println("ID: " +rs.getString("id_autor") + "\tNom: " + rs.getString("nom") + "\tAny Naixement: " + rs.getString("any_naixement") + "\tNacionalitat: " + rs.getString("nacionalitat") + "\tActiu: " + rs.getString("actiu"));
		rs.close();
		st.close();
	}

	public void mostraRevistes() throws SQLException, IOException {
		Statement st = conn.createStatement();
		Scanner reader = new Scanner(System.in);
		ResultSet rs;

		rs = st.executeQuery("SELECT * FROM revistes");
		while (rs.next()) System.out.println("ID: " +rs.getString(1) + "\tTitol: " + rs.getString(2) + "\tData Publicacio: " + rs.getString(3));
		rs.close();
		st.close();
	}

	public void mostraRevistesArticlesAutors() throws SQLException, IOException {
		Statement st = conn.createStatement();
		Scanner reader = new Scanner(System.in);
		ResultSet rs;

		rs = st.executeQuery("SELECT a.nom, r.titol, ar.titol FROM autors a, revistes r, articles ar WHERE ar.id_autor=a.id_autor AND ar.id_revista=r.id_revista");
		while (rs.next()) System.out.println("Nom autor: " +rs.getString(1) + "\tNomRevista: " + rs.getString(2) + "\tNom article: " + rs.getString(3));
		rs.close();
		st.close();
	}

	public void sortir() throws SQLException {
		System.out.println("ADÉU!");
		conn.close();
	}
	
	public void carregaAutors(Connection conn) throws SQLException, NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader("autors.csv"));
		String sqlInsert = "INSERT INTO autors (id_autor, nom, any_naixement, nacionalitat, actiu) VALUES (?,?,?,?,?)";
		PreparedStatement pst = conn.prepareStatement(sqlInsert);

		for (int i = 0; i < 3; i++) {
			pst.clearParameters();
			String linea = br.readLine();
			String[] datos = linea.split(",");
			int id = Integer.parseInt(datos[0]);
			String nom = datos[1];
			int año = Integer.parseInt(datos[2]);
			String nacionalidad = datos[3];
			String actiu = datos[4];

			pst.setInt(1, id);
			pst.setString(2, nom);
			pst.setInt(3, año);
			pst.setString(4, nacionalidad);
			pst.setString(5, actiu);
			pst.executeUpdate();

		}
	}
}
