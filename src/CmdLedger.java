import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class CmdLedger {

	public static String readFile(String nameAndExtension) {
		String importer = "";
		Scanner fi = null;
		try {
			fi = new Scanner(new File(nameAndExtension));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		while (fi.hasNextLine()) {
			importer += fi.nextLine();
			importer += "\n";
		}
		importer.substring(0, importer.length() - 1); // strip trailing whitespace.
		importer.trim();
		return importer;
	}

	public static void main(String[] args){
		Ledger session = new Ledger();
		Scanner in = new Scanner(System.in);
		while (true) {
			String cmd = in.nextLine();
			if (cmd.equalsIgnoreCase("e")) {
				break;
			}
			// check for exit.
			if (cmd.equalsIgnoreCase("p")) {
				System.out.println(session);
			} // Print ledger
			if (cmd.equalsIgnoreCase("w")) {
				session.wipe();
			}
			if (cmd.equalsIgnoreCase("h")) {
				System.out.println(
						"[F]ile:  Supply filename:<infilename>.  Read in a file of transactions. Any invalid transaction shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be “Error: file <infilename> cannot be opened for reading” on a single line, where <infilename> is the name provided as additional command input.  \r\n"
								+ "\r\n"
								+ "		[T]ransaction: Supply Transaction:<see format below>   Read in a single transaction in the format shown below.  It shall be checked for validity against the ledger, and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the transaction number followed by a colon, a space, and the reason it is invalid on a single line.\r\n"
								+ "\r\n" + "		[E]xit:  Quit the program\r\n" + "\r\n"
								+ "		[P]rint:  Print current ledger (all transactions in the order they were added) to stdout in the transaction format given below, one transaction per line.\r\n"
								+ "\r\n" + "		[H]elp:  Command Summary\r\n" + "\r\n"
								+ "		[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be “Error: file <outfilename> cannot be opened for writing” on a single line, where <outfilename> is the name provided as additional command input. \r\n"
								+ "\r\n" + "		[W]ipe:  Wipe the entire ledger to start fresh.\r\n" + "\r\n"
								+ "		[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\r\n"
								+ "\r\n"
								+ "		[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each transaction number as it is read in, followed by a colon, a space, and the result (“good” or “bad”). \r\n"
								+ "\r\n"
								+ "		[B]alance:  Supply username:  (e.g. Alice).  This command prints the current balance of a user.");
			}
			if (cmd.equalsIgnoreCase("v")) {
				session.setVerbose(!session.isVerbose());
				System.out.println("Ledger set to verbose mode = " + session.isVerbose());
			}
			if (cmd.equalsIgnoreCase("i")) {
				session.setInteractive(!session.isInteractive());
				System.out.println("Ledger set to interactive mode = " + session.isInteractive());
			}
			// At this point we have possible options:
			// F, T, B, D which require special parsing.
			// parse response
			char firstChar = cmd.toLowerCase().charAt(0);
			String remainingCmd = cmd.substring(1).trim();//Drop first char and any whitespace.
			switch (firstChar) {
			case 'f':
				try {
					loadFromFile(session, remainingCmd);
				} catch (FileNotFoundException e) {
					System.err.println("File Not Found.");
				}
				break;
			case 't':
					session.addTransaction(remainingCmd);
				break;
			case 'b':
				try {
					System.out.println(session.calcBalance(remainingCmd.trim()));
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 'd':
				try {
					dumpFile(session, remainingCmd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}

			// execute library command.

		}
		/*
		 * [F]ile: Supply filename:<infilename>. Read in a file of transactions. Any
		 * invalid transaction shall be identified with an error message to stderr, but
		 * not stored. Print an error message to stderr if the input file named cannot
		 * be opened. The message shall be “Error: file <infilename> cannot be opened
		 * for reading” on a single line, where <infilename> is the name provided as
		 * additional command input.
		 * 
		 * [T]ransaction: Supply Transaction:<see format below> Read in a single
		 * transaction in the format shown below. It shall be checked for validity
		 * against the ledger, and added if it is valid. If it is not valid, then do not
		 * add it to the ledger and print a message to stderr with the transaction
		 * number followed by a colon, a space, and the reason it is invalid on a single
		 * line.
		 * 
		 * [D]ump: Supply filename:<outfilename>. Dump ledger to the named file. Print
		 * an error message to stderr if the output file named cannot be opened. The
		 * message shall be “Error: file <outfilename> cannot be opened for writing” on
		 * a single line, where <outfilename> is the name provided as additional command
		 * input.
		 * 
		 * [B]alance: Supply username: (e.g. Alice). This command prints the current
		 * balance of a user.
		 */
	}


	private static void dumpFile(Ledger session, String remainingCmd) throws IOException {
		FileOutputStream fs = new FileOutputStream(new File(remainingCmd.trim()));
		for(Entry e : session.getBlockchain()) {
			fs.write(e.toString().getBytes());
			fs.write("\n".getBytes());
		}
		fs.flush();
		fs.close();
	}

	private static void loadFromFile(Ledger session, String remainingCmd) throws FileNotFoundException {
		session.wipe();
		Scanner fi = new Scanner(new File(remainingCmd));
		while(fi.hasNextLine()) {
			session.addTransaction(fi.nextLine());
		}
	}
	
	
}
