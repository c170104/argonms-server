/*
 * ArgonMS MapleStory server emulator written in Java
 * Copyright (C) 2011  GoldenKevin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package argonms.tools.input;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A copy of DatabaseConnection that connects to the MCDB (Maple Community
 * Database) schema for game data loading instead of the account/character stats
 * schema to load and save characters.
 * 
 * @author GoldenKevin
 * @version 1.0
 */
public class WzDatabaseConnection {
	private final static Logger LOG = Logger.getLogger(WzDatabaseConnection.class.getName());

	private static ThreadLocal<Connection> con = new ThreadLocalConnection();
	private static Properties props;

	public static Connection getConnection() {
		if (props == null) throw new RuntimeException("WzDatabaseConnection not initialized");
		return con.get();
	}

	public static boolean isInitialized() {
		return props != null;
	}

	public static void setProps(Properties aProps) {
		props = aProps;
	}

	public static void closeAll() throws SQLException {
		for (Connection con : ThreadLocalConnection.allConnections) {
			con.close();
		}
	}

	private static class ThreadLocalConnection extends ThreadLocal<Connection> {
		public static Collection<Connection> allConnections = new LinkedList<Connection>();

		@Override
		protected Connection initialValue() {
			String driver = props.getProperty("driver");
			String url = props.getProperty("mcdb");
			String user = props.getProperty("user");
			String password = props.getProperty("password");
			try {
				Class.forName(driver); // touch the mysql driver
			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, "Could not find JDBC library. Do you have MySQL Connector/J?", e);
			}
			try {
				Connection con = DriverManager.getConnection(url, user, password);
				allConnections.add(con);
				LOG.log(Level.INFO, "New database connection created. {0} have been created.", allConnections.size());
				return con;
			} catch (SQLException e) {
				LOG.log(Level.SEVERE, "Could not connect to the database.", e);
				return null;
			}
		}
	}
}