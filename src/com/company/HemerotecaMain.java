package com.company;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

public class HemerotecaMain {

	public static void main(String[] args) throws IOException, SQLException, ParseException {
		Menu menu = new Menu();
		Connection conn = null;
		Identity identity;
		int option;
		int intents = 0;
		DBAccessor dbaccessor = new DBAccessor();
		dbaccessor.init();
		while (intents < 3 && conn == null) {
			identity = menu.autenticacio(intents);
			// prova de test
			identity.toString();
			
			conn = dbaccessor.getConnection(identity);
			intents++;
		}

		option = menu.menuPral();
		while (option > 0 && option < 12) {
			switch (option) {
				case 1 -> dbaccessor.mostraAutors();
				case 2 -> dbaccessor.mostraRevistes();
				case 3 -> dbaccessor.mostraRevistesArticlesAutors();
				case 4 -> dbaccessor.altaAutor();
				case 5 -> dbaccessor.altaRevista();
				case 6 -> dbaccessor.altaArticle();
				case 7 -> dbaccessor.actualitzarTitolRevistes(conn);
				case 8 -> dbaccessor.afegeixArticleARevista(conn);
				case 9 -> dbaccessor.desassignaArticleARevista(conn);
				case 10 -> dbaccessor.carregaAutors(conn);
				case 11 -> dbaccessor.sortir();
				default -> System.out.println("Introdueixi una de les opcions anteriors");
			}
			option = menu.menuPral();
		}

	}

}
