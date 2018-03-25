import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class CmdLedger {

	public static void main(String[] args) {
		Ledger session = Ledger.getInstance();
		Scanner in = new Scanner(System.in);
		while (true) {
			if (session.isInteractive()) {
				System.out.println(
						"Give a command! [H]elp, [E]xit, [P]rint, [W]ipe, [B]alance, [T]x [V]erbose, [I]nteractive, [F]ile, [D]ump, [O]utput, [R]ead, [C]heck");
			}
			String cmd = in.nextLine();
			if (session.isInteractive()) {
				switch (cmd) {
				case "f":
					if (session.isVerbose()) {
						System.out.println("File load initiated!");
					}
					System.out.println("Which filename (full extension)");
					cmd += " " + in.nextLine();
					break;
				case "t":
					if (session.isVerbose()) {
						System.out.println("Transaction add initiated!");
						if(session.getBlockchain().size() == 0) {
							System.out.println("Your blockchain is empty and needs a genesis transaction.");
							System.out.println("Please add a transaction with no inputs, like \"root; 0; ; 1; (God, 1000)\"");
							System.out.println("Please note that unbalanced transactions of this type are not usually allowed.");
						}
					}
					System.out.println("Which transaction name?");
					cmd += " " + in.nextLine() + "; ";
					System.out.println("How many transaction inputs?");
					cmd += " " + in.nextLine() + "; ";
					System.out.println("Give inputs in format (txid, index)(txid, index)");
					cmd += " " + in.nextLine() + "; ";
					System.out.println("How many transaction outputs?");
					cmd += " " + in.nextLine() + "; ";
					System.out.println("Give outputs in format (name, amount)(name, amount)");
					cmd += " " + in.nextLine();
					break;
				case "b":
					if (session.isVerbose()) {
						System.out.println("Balance check initiated!");
					}
					System.out.println("Which user balance to check?");
					cmd += " " + in.nextLine();
					break;
				case "d":
					if (session.isVerbose()) {
						System.out.println("File store initiated!");
					}
					System.out.println("Which filename (full extension)");
					cmd += " " + in.nextLine();
					break;
				default:
					if (session.isVerbose()) {
						System.out.println("Executing command " + cmd);
					}

				}
			}
			if (cmd.equalsIgnoreCase("e")) {
				if(session.isVerbose()) {
					System.out.println("Exiting!");
				}
				break;
			}
			// check for exit.
			if (cmd.equalsIgnoreCase("p")) {
				System.out.println(session);
			} // Print ledger
			if (cmd.equalsIgnoreCase("w")) {
				if(session.isVerbose()) {
					System.out.println("Session wiping!");
				}
				session.wipe();
			}
			if (cmd.equalsIgnoreCase("h")) {
				System.out.println(
						"[F]ile:  Supply filename:<infilename>.  Read in a file of transactions. Any invalid transaction shall be identified with an error message to stderr, but not stored. Print an error message to stderr if the input file named cannot be opened. The message shall be Error: file <infilename> cannot be opened for reading on a single line, where <infilename> is the name provided as additional command input.  \r\n"
								+ "\r\n"
								+ "[T]ransaction: Supply Transaction:<see format below>   Read in a single transaction in the format shown below.  It shall be checked for validity against the ledger, and added if it is valid. If it is not valid, then do not add it to the ledger and print a message to stderr with the transaction number followed by a colon, a space, and the reason it is invalid on a single line.\r\n"
								+ "\r\n" + "[E]xit:  Quit the program\r\n" + "\r\n"
								+ "[P]rint:  Print current ledger (all transactions in the order they were added) to stdout in the transaction format given below, one transaction per line.\r\n"
								+ "\r\n" + "[H]elp:  Command Summary\r\n" + "\r\n"
								+ "[D]ump:  Supply filename:<outfilename>.  Dump ledger to the named file. Print an error message to stderr if the output file named cannot be opened. The message shall be Error: file <outfilename> cannot be opened for writing on a single line, where <outfilename> is the name provided as additional command input. \r\n"
								+ "\r\n" + "[W]ipe:  Wipe the entire ledger to start fresh.\r\n" + "\r\n"
								+ "[I]nteractive: Toggle interactive mode. Start in non-interactive mode, where no command prompts are printed. Print command prompts and prompts for additional input in interactive mode, starting immediately (i.e., print a command prompt following the I command).\r\n"
								+ "\r\n"
								+ "[V]erbose: Toggle verbose mode. Start in non-verbose mode. In verbose mode, print additional diagnostic information as you wish. At all times, output each transaction number as it is read in, followed by a colon, a space, and the result (good or bad). \r\n"
								+ "\r\n"
								+ "[B]alance:  Supply username:  (e.g. Alice).  This command prints the current balance of a user."
								+ "\r\n"
								+ "[O]utput:  collect all correctly signed transactions that have not been output in a previous transaction block and output them as a transaction block.  This outputs the current block only."
								+ "\r\n"
								+ "[R]ead:  supply <account name> <keyfilename>. <account name is the name of the account associated with the key ."
								+ "\r\n"
								+ "[C]heck:  Supply <transactionID>:  The signature of the signed transaction (in the two-line format given above) shall be checked. Output OK to stdout if good, else output Bad to stdout. If bad, output additional diagnostic information to stderr."
								);
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
			String remainingCmd = cmd.substring(1).trim();// Drop first char and any whitespace.
			switch (firstChar) {
			case 'f':
				try {
					loadFromFile(session, remainingCmd);
				} catch (FileNotFoundException e) {
					System.err.println("Error: file " + remainingCmd.trim() + " cannot be opened for reading");
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
					e.printStackTrace();
				}
				break;
			case 'o':
				/*
				 * collect all correctly signed transactions that have not been output in a previous transaction block and output them as a transaction block.  This outputs the current block only.  This includes outputting a line with a single integer indicating the number of signed transactions that follow, followed by those transactions.
				 */
				//TODO
				break;
			case 'c':
				//param txid, output OK or Bad.
				break;
			case 'r':
				String[] accAndFileName = remainingCmd.split(" ");
				String accountname = accAndFileName[0].trim();
				String keyfile = accAndFileName[1].trim();
				List<Identity> list= Identity.getPeople();
				boolean found = false;
				for(Identity i: list) {
					if(i.getName().equals(accountname)) {
						i.loadKeyPair(keyfile);
						found = true;
						break;
					}
				}
				if(!found) {
					Identity created = new Identity(accountname);
					created.loadKeyPair(keyfile);
				}
				break;
			}

			// execute library command.

		}
	}

	public static void dumpFile(Ledger session, String remainingCmd) throws IOException {
		FileOutputStream fs = new FileOutputStream(new File(remainingCmd.trim()));
		for (Entry e : session.getBlockchain()) {
			fs.write(e.toString().getBytes());
			fs.write("\n".getBytes());
		}
		fs.flush();
		fs.close();
		if(session.isVerbose()) {
			System.out.println("File write complete!");
		}
	}

	public static void loadFromFile(Ledger session, String remainingCmd) throws FileNotFoundException {
		session.wipe();
		Scanner fi = new Scanner(new File(remainingCmd));
		while (fi.hasNextLine()) {
			session.addTransaction(fi.nextLine());
		}
		if(session.isVerbose()) {
			System.out.println("File load complete!");
		}
	}

}
