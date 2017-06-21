package sc.plugin2018.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Condition;

import sc.shared.PlayerColor;

import sc.plugin2018.*;
import sc.protocol.LobbyProtocol;

import com.thoughtworks.xstream.XStream;

public class Configuration
{
	private static XStream	xStream;

	static
	{
		xStream = new XStream();
		xStream.setMode(XStream.NO_REFERENCES);
		xStream.setClassLoader(Configuration.class.getClassLoader());
		LobbyProtocol.registerMessages(xStream);
		LobbyProtocol.registerAdditionalMessages(xStream,
				getClassesToRegister());
		GameState state = new GameState();
		System.out.println(xStream.toXML(state).toString());
		System.out.println(xStream.toXML(state.getBluePlayer()));
	}

	public static XStream getXStream()
	{
		return xStream;
	}

	public static List<Class<?>> getClassesToRegister()
	{
		return Arrays.asList(new Class<?>[] { Game.class, Board.class,
				GameState.class, Move.class, Player.class,
				WelcomeMessage.class, CardType.class, FieldType.class,
				Position.class, Advance.class, Action.class, Skip.class, PlayerColor.class,
				Card.class, EatSalad.class, ExchangeCarrots.class, FallBack.class, Condition.class});
	} // TODO why Condition (seen in previous plugin)
}
