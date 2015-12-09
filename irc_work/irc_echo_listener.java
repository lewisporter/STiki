package irc_work;

import org.schwering.irc.lib.IRCEventListener;
import org.schwering.irc.lib.IRCModeParser;
import org.schwering.irc.lib.IRCUser;

/**
 * Andrew G. West - irc_echo_listener.java - This is a straightforward
 * IRCEventListener. All event triggers output to STDOUT, making this
 * an IRC class useful for debugging purposes.
 */
public class irc_echo_listener implements IRCEventListener {

	// **************************** PUBLIC METHODS ***************************

	// All javadoc will be provided in a super-overriding fashion.
	// All we do here is output the event triggers in String format.

	@Override
	public void onRegistered() {
		print("Connected");
	}

	@Override
	public void onDisconnected() {
		print("Disconnected");
	}

	@Override
	public void onError(String msg) {
		print("Error: " + msg);
	}

	@Override
	public void onError(int num, String msg) {
		print("Error #" + num + ": " + msg);
	}

	@Override
	public void onInvite(String chan, IRCUser u, String nickPass) {
		print(chan + "> " + u.getNick() + " invites " + nickPass);
	}

	@Override
	public void onJoin(String chan, IRCUser u) {
		print(chan + "> " + u.getNick() + " joins");
	}

	@Override
	public void onKick(String chan, IRCUser u, String nickPass, String msg) {
		print(chan + "> " + u.getNick() + " kicks " + nickPass);
	}

	@Override
	public void onMode(IRCUser u, String nickPass, String mode) {
		print("Mode: " + u.getNick() + " sets modes " + mode + " " + nickPass);
	}

	@Override
	public void onMode(String chan, IRCUser u, IRCModeParser mp) {
		print(chan + "> " + u.getNick() + " sets mode: " + mp.getLine());
	}

	@Override
	public void onNick(IRCUser u, String nickNew) {
		print("Nick: " + u.getNick() + " is now known as " + nickNew);
	}

	@Override
	public void onNotice(String target, IRCUser u, String msg) {
		print(target + "> " + u.getNick() + " (notice): " + msg);
	}

	@Override
	public void onPart(String chan, IRCUser u, String msg) {
		print(chan + "> " + u.getNick() + " parts");
	}

	@Override
	public void onPrivmsg(String chan, IRCUser u, String msg) {
		print(chan + "> " + u.getNick() + ": " + msg);
	}

	@Override
	public void onQuit(IRCUser u, String msg) {
		print("Quit: " + u.getNick());
	}

	@Override
	public void onReply(int num, String value, String msg) {
		print("Reply #" + num + ": " + value + " " + msg);
	}

	@Override
	public void onTopic(String chan, IRCUser u, String topic) {
		print(chan + "> " + u.getNick() + " changes topic into: " + topic);
	}

	@Override
	public void onPing(String p) {
	}

	@Override
	public void unknown(String a, String b, String c, String d) {
		print("UNKNOWN: " + a + " b " + c + " " + d);
	}


	// *************************** PRIVATE METHODS ***************************

	/**
	 * Output some text to a line (a shortcut for System.out.println()).
	 *
	 * @param str String data to be output to STDOUT
	 */
	private void print(String str) {
		System.out.println(str);
		System.out.flush();
	}
}