// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// This is unreleased code

package com.google.appinventor.shared.util;

/**
 * Routines to facilitate various account creation and management functions.
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

import java.util.Arrays;
import java.util.Random;

public class AccountUtil {

  private static long mask = 0x7FF;  // 11 bits

  private static Random random = new Random();

  private static long MAX = 17592186044415L;

  private AccountUtil() {       /* Cannot be instantiated */
  };

  private static String makeWordList(long input) {
    if (input < 0 || input > MAX) {
      throw new IllegalArgumentException("makeWordList: input out of range " + input);
    }
    int A = (int)(input & mask);
    int B = (int)((input >>> 11) & mask);
    int C = (int)((input >>> 22) & mask);
    int D = (int)((input >>> 33) & mask);
    StringBuilder sb = new StringBuilder();
    sb.append(words[A]);
    sb.append("-");
    sb.append(words[B]);
    sb.append("-");
    sb.append(words[C]);
    sb.append("-");
    sb.append(words[D]);
    return sb.toString();
  }

  private static long parseWordList(String input) {
    String [] p = input.split("-");
    if (p.length != 4) {
      throw new IllegalArgumentException("parseWordList: invalid input " + input);
    }
    long acc = 0;
    for (int i = 0; i < 4; i++) {
      int index = Arrays.binarySearch(words, p[3-i]);
      if (index < 0) {
        throw new IllegalArgumentException("parseWordList: invalid input (word not found): " + p[3-i] + " " + index);
      }
/*      System.out.println(String.format("0x%016X", acc) + " " + String.format("0x%08X", index)); */
      acc <<= 11;
      acc |= index;
    }
    return acc;
  }

  public static String codeToAccount(String code) {
    long accountId = parseWordList(code);
    return "anon-" + (new Long(accountId)).toString();
  }

  public static String accountToCode(String account) {
    if (!account.startsWith("anon-")) {
      throw new IllegalArgumentException("Invalid Account Identifier: " + account);
    }
    long accountId = Long.parseLong(account.substring(5));
    return makeWordList(accountId);
  }

  public static String generateAccountId() {
    return "anon-" + getRandom();
  }

  private static long getRandom() {
    synchronized(AccountUtil.class) {
      while(true) {
        long witness = random.nextLong();
        if (witness < 0 || witness > MAX) // nothing bigger then 2^45-1
          continue;
        return witness;
      }
    }
  }

  private static String [] words = {
    "A", "ABE", "ABED", "ABEL", "ABET", "ABLE", "ABUT", "ACE", "ACHE",
    "ACID", "ACME", "ACRE", "ACT", "ACTA", "ACTS", "AD", "ADA", "ADAM",
    "ADD", "ADDS", "ADEN", "AFAR", "AFRO", "AGEE", "AGO", "AHEM", "AHOY",
    "AID", "AIDA", "AIDE", "AIDS", "AIM", "AIR", "AIRY", "AJAR", "AKIN",
    "ALAN", "ALEC", "ALGA", "ALIA", "ALL", "ALLY", "ALMA", "ALOE", "ALP",
    "ALSO", "ALTO", "ALUM", "ALVA", "AM", "AMEN", "AMES", "AMID", "AMMO",
    "AMOK", "AMOS", "AMRA", "AMY", "AN", "ANA", "AND", "ANDY", "ANEW",
    "ANN", "ANNA", "ANNE", "ANT", "ANTE", "ANTI", "ANY", "APE", "APS",
    "APT", "AQUA", "ARAB", "ARC", "ARCH", "ARE", "AREA", "ARGO", "ARID",
    "ARK", "ARM", "ARMY", "ART", "ARTS", "ARTY", "AS", "ASH", "ASIA",
    "ASK", "ASKS", "AT", "ATE", "ATOM", "AUG", "AUK", "AUNT", "AURA",
    "AUTO", "AVE", "AVER", "AVID", "AVIS", "AVON", "AVOW", "AWAY", "AWE",
    "AWK", "AWL", "AWN", "AWRY", "AX", "AYE", "BABE", "BABY", "BACH",
    "BACK", "BAD", "BADE", "BAG", "BAH", "BAIL", "BAIT", "BAKE", "BALD",
    "BALE", "BALI", "BALK", "BALL", "BALM", "BAM", "BAN", "BAND", "BANE",
    "BANG", "BANK", "BAR", "BARB", "BARD", "BARE", "BARK", "BARN", "BARR",
    "BASE", "BASH", "BASK", "BASS", "BAT", "BATE", "BATH", "BAWD", "BAWL",
    "BAY", "BE", "BEAD", "BEAK", "BEAM", "BEAN", "BEAR", "BEAT", "BEAU",
    "BECK", "BED", "BEE", "BEEF", "BEEN", "BEER", "BEET", "BEG", "BELA",
    "BELL", "BELT", "BEN", "BEND", "BENT", "BERG", "BERN", "BERT", "BESS",
    "BEST", "BET", "BETA", "BETH", "BEY", "BHOY", "BIAS", "BIB", "BID",
    "BIDE", "BIEN", "BIG", "BILE", "BILK", "BILL", "BIN", "BIND", "BING",
    "BIRD", "BIT", "BITE", "BITS", "BLAB", "BLAT", "BLED", "BLEW", "BLOB",
    "BLOC", "BLOT", "BLOW", "BLUE", "BLUM", "BLUR", "BOAR", "BOAT", "BOB",
    "BOCA", "BOCK", "BODE", "BODY", "BOG", "BOGY", "BOHR", "BOIL", "BOLD",
    "BOLO", "BOLT", "BOMB", "BON", "BONA", "BOND", "BONE", "BONG", "BONN",
    "BONY", "BOO", "BOOK", "BOOM", "BOON", "BOOT", "BOP", "BORE", "BORG",
    "BORN", "BOSE", "BOSS", "BOTH", "BOUT", "BOW", "BOWL", "BOY", "BOYD",
    "BRAD", "BRAE", "BRAG", "BRAN", "BRAY", "BRED", "BREW", "BRIG",
    "BRIM", "BROW", "BUB", "BUCK", "BUD", "BUDD", "BUFF", "BUG", "BULB",
    "BULK", "BULL", "BUM", "BUN", "BUNK", "BUNT", "BUOY", "BURG", "BURL",
    "BURN", "BURR", "BURT", "BURY", "BUS", "BUSH", "BUSS", "BUST", "BUSY",
    "BUT", "BUY", "BY", "BYE", "BYTE", "CAB", "CADY", "CAFE", "CAGE",
    "CAIN", "CAKE", "CAL", "CALF", "CALL", "CALM", "CAM", "CAME", "CAN",
    "CANE", "CANT", "CAP", "CAR", "CARD", "CARE", "CARL", "CARR", "CART",
    "CASE", "CASH", "CASK", "CAST", "CAT", "CAVE", "CAW", "CEIL", "CELL",
    "CENT", "CERN", "CHAD", "CHAR", "CHAT", "CHAW", "CHEF", "CHEN",
    "CHEW", "CHIC", "CHIN", "CHOU", "CHOW", "CHUB", "CHUG", "CHUM",
    "CITE", "CITY", "CLAD", "CLAM", "CLAN", "CLAW", "CLAY", "CLOD",
    "CLOG", "CLOT", "CLUB", "CLUE", "COAL", "COAT", "COCA", "COCK",
    "COCO", "COD", "CODA", "CODE", "CODY", "COED", "COG", "COIL", "COIN",
    "COKE", "COL", "COLA", "COLD", "COLT", "COMA", "COMB", "COME", "CON",
    "COO", "COOK", "COOL", "COON", "COOT", "COP", "CORD", "CORE", "CORK",
    "CORN", "COST", "COT", "COVE", "COW", "COWL", "COY", "CRAB", "CRAG",
    "CRAM", "CRAY", "CREW", "CRIB", "CROW", "CRUD", "CRY", "CUB", "CUBA",
    "CUBE", "CUE", "CUFF", "CULL", "CULT", "CUNY", "CUP", "CUR", "CURB",
    "CURD", "CURE", "CURL", "CURT", "CUT", "CUTS", "DAB", "DAD", "DADE",
    "DALE", "DAM", "DAME", "DAN", "DANA", "DANE", "DANG", "DANK", "DAR",
    "DARE", "DARK", "DARN", "DART", "DASH", "DATA", "DATE", "DAVE",
    "DAVY", "DAWN", "DAY", "DAYS", "DEAD", "DEAF", "DEAL", "DEAN", "DEAR",
    "DEBT", "DECK", "DEE", "DEED", "DEEM", "DEER", "DEFT", "DEFY", "DEL",
    "DELL", "DEN", "DENT", "DENY", "DES", "DESK", "DEW", "DIAL", "DICE",
    "DID", "DIE", "DIED", "DIET", "DIG", "DIME", "DIN", "DINE", "DING",
    "DINT", "DIP", "DIRE", "DIRT", "DISC", "DISH", "DISK", "DIVE", "DO",
    "DOCK", "DOE", "DOES", "DOG", "DOLE", "DOLL", "DOLT", "DOME", "DON",
    "DONE", "DOOM", "DOOR", "DORA", "DOSE", "DOT", "DOTE", "DOUG", "DOUR",
    "DOVE", "DOW", "DOWN", "DRAB", "DRAG", "DRAM", "DRAW", "DREW", "DRUB",
    "DRUG", "DRUM", "DRY", "DUAL", "DUB", "DUCK", "DUCT", "DUD", "DUE",
    "DUEL", "DUET", "DUG", "DUKE", "DULL", "DUMB", "DUN", "DUNE", "DUNK",
    "DUSK", "DUST", "DUTY", "EACH", "EAR", "EARL", "EARN", "EASE", "EAST",
    "EASY", "EAT", "EBEN", "ECHO", "ED", "EDDY", "EDEN", "EDGE", "EDGY",
    "EDIT", "EDNA", "EEL", "EGAN", "EGG", "EGO", "ELAN", "ELBA", "ELI",
    "ELK", "ELLA", "ELM", "ELSE", "ELY", "EM", "EMIL", "EMIT", "EMMA",
    "END", "ENDS", "ERIC", "EROS", "EST", "ETC", "EVA", "EVE", "EVEN",
    "EVER", "EVIL", "EWE", "EYE", "EYED", "FACE", "FACT", "FAD", "FADE",
    "FAIL", "FAIN", "FAIR", "FAKE", "FALL", "FAME", "FAN", "FANG", "FAR",
    "FARM", "FAST", "FAT", "FATE", "FAWN", "FAY", "FEAR", "FEAT", "FED",
    "FEE", "FEED", "FEEL", "FEET", "FELL", "FELT", "FEND", "FERN", "FEST",
    "FEUD", "FEW", "FIB", "FIEF", "FIG", "FIGS", "FILE", "FILL", "FILM",
    "FIN", "FIND", "FINE", "FINK", "FIR", "FIRE", "FIRM", "FISH", "FISK",
    "FIST", "FIT", "FITS", "FIVE", "FLAG", "FLAK", "FLAM", "FLAT", "FLAW",
    "FLEA", "FLED", "FLEW", "FLIT", "FLO", "FLOC", "FLOG", "FLOW", "FLUB",
    "FLUE", "FLY", "FOAL", "FOAM", "FOE", "FOG", "FOGY", "FOIL", "FOLD",
    "FOLK", "FOND", "FONT", "FOOD", "FOOL", "FOOT", "FOR", "FORD", "FORE",
    "FORK", "FORM", "FORT", "FOSS", "FOUL", "FOUR", "FOWL", "FRAU",
    "FRAY", "FRED", "FREE", "FRET", "FREY", "FROG", "FROM", "FRY", "FUEL",
    "FULL", "FUM", "FUME", "FUN", "FUND", "FUNK", "FUR", "FURY", "FUSE",
    "FUSS", "GAB", "GAD", "GAFF", "GAG", "GAGE", "GAIL", "GAIN", "GAIT",
    "GAL", "GALA", "GALE", "GALL", "GALT", "GAM", "GAME", "GANG", "GAP",
    "GARB", "GARY", "GAS", "GASH", "GATE", "GAUL", "GAUR", "GAVE", "GAWK",
    "GAY", "GEAR", "GEE", "GEL", "GELD", "GEM", "GENE", "GENT", "GERM",
    "GET", "GETS", "GIBE", "GIFT", "GIG", "GIL", "GILD", "GILL", "GILT",
    "GIN", "GINA", "GIRD", "GIRL", "GIST", "GIVE", "GLAD", "GLEE", "GLEN",
    "GLIB", "GLOB", "GLOM", "GLOW", "GLUE", "GLUM", "GLUT", "GO", "GOAD",
    "GOAL", "GOAT", "GOER", "GOES", "GOLD", "GOLF", "GONE", "GONG",
    "GOOD", "GOOF", "GORE", "GORY", "GOSH", "GOT", "GOUT", "GOWN", "GRAB",
    "GRAD", "GRAY", "GREG", "GREW", "GREY", "GRID", "GRIM", "GRIN",
    "GRIT", "GROW", "GRUB", "GULF", "GULL", "GUM", "GUN", "GUNK", "GURU",
    "GUS", "GUSH", "GUST", "GUT", "GUY", "GWEN", "GWYN", "GYM", "GYP",
    "HA", "HAAG", "HAAS", "HACK", "HAD", "HAIL", "HAIR", "HAL", "HALE",
    "HALF", "HALL", "HALO", "HALT", "HAM", "HAN", "HAND", "HANG", "HANK",
    "HANS", "HAP", "HARD", "HARK", "HARM", "HART", "HAS", "HASH", "HAST",
    "HAT", "HATE", "HATH", "HAUL", "HAVE", "HAW", "HAWK", "HAY", "HAYS",
    "HE", "HEAD", "HEAL", "HEAR", "HEAT", "HEBE", "HECK", "HEED", "HEEL",
    "HEFT", "HELD", "HELL", "HELM", "HEM", "HEN", "HER", "HERB", "HERD",
    "HERE", "HERO", "HERS", "HESS", "HEW", "HEWN", "HEY", "HI", "HICK",
    "HID", "HIDE", "HIGH", "HIKE", "HILL", "HILT", "HIM", "HIND", "HINT",
    "HIP", "HIRE", "HIS", "HISS", "HIT", "HIVE", "HO", "HOB", "HOBO",
    "HOC", "HOCK", "HOE", "HOFF", "HOG", "HOLD", "HOLE", "HOLM", "HOLT",
    "HOME", "HONE", "HONK", "HOOD", "HOOF", "HOOK", "HOOT", "HOP", "HORN",
    "HOSE", "HOST", "HOT", "HOUR", "HOVE", "HOW", "HOWE", "HOWL", "HOYT",
    "HUB", "HUCK", "HUE", "HUED", "HUFF", "HUG", "HUGE", "HUGH", "HUGO",
    "HUH", "HULK", "HULL", "HUM", "HUNK", "HUNT", "HURD", "HURL", "HURT",
    "HUSH", "HUT", "HYDE", "HYMN", "I", "IBIS", "ICON", "ICY", "IDA",
    "IDEA", "IDLE", "IF", "IFFY", "IKE", "ILL", "INCA", "INCH", "INK",
    "INN", "INTO", "IO", "ION", "IONS", "IOTA", "IOWA", "IQ", "IRA",
    "IRE", "IRIS", "IRK", "IRMA", "IRON", "IS", "ISLE", "IT", "ITCH",
    "ITEM", "ITS", "IVAN", "IVY", "JAB", "JACK", "JADE", "JAG", "JAIL",
    "JAKE", "JAM", "JAN", "JANE", "JAR", "JAVA", "JAW", "JAY", "JEAN",
    "JEFF", "JERK", "JESS", "JEST", "JET", "JIBE", "JIG", "JILL", "JILT",
    "JIM", "JIVE", "JO", "JOAN", "JOB", "JOBS", "JOCK", "JOE", "JOEL",
    "JOEY", "JOG", "JOHN", "JOIN", "JOKE", "JOLT", "JOT", "JOVE", "JOY",
    "JUDD", "JUDE", "JUDO", "JUDY", "JUG", "JUJU", "JUKE", "JULY", "JUNE",
    "JUNK", "JUNO", "JURY", "JUST", "JUT", "JUTE", "KAHN", "KALE", "KANE",
    "KANT", "KARL", "KATE", "KAY", "KEEL", "KEEN", "KEG", "KEN", "KENO",
    "KENT", "KERN", "KERR", "KEY", "KEYS", "KICK", "KID", "KILL", "KIM",
    "KIN", "KIND", "KING", "KIRK", "KISS", "KIT", "KITE", "KLAN", "KNEE",
    "KNEW", "KNIT", "KNOB", "KNOT", "KNOW", "KOCH", "KONG", "KUDO",
    "KURD", "KURT", "KYLE", "LA", "LAB", "LAC", "LACE", "LACK", "LACY",
    "LAD", "LADY", "LAG", "LAID", "LAIN", "LAIR", "LAKE", "LAM", "LAMB",
    "LAME", "LAND", "LANE", "LANG", "LAP", "LARD", "LARK", "LASS", "LAST",
    "LATE", "LAUD", "LAVA", "LAW", "LAWN", "LAWS", "LAY", "LAYS", "LEA",
    "LEAD", "LEAF", "LEAK", "LEAN", "LEAR", "LED", "LEE", "LEEK", "LEER",
    "LEFT", "LEG", "LEN", "LEND", "LENS", "LENT", "LEO", "LEON", "LESK",
    "LESS", "LEST", "LET", "LETS", "LEW", "LIAR", "LICE", "LICK", "LID",
    "LIE", "LIED", "LIEN", "LIES", "LIEU", "LIFE", "LIFT", "LIKE", "LILA",
    "LILT", "LILY", "LIMA", "LIMB", "LIME", "LIN", "LIND", "LINE", "LINK",
    "LINT", "LION", "LIP", "LISA", "LIST", "LIT", "LIVE", "LO", "LOAD",
    "LOAF", "LOAM", "LOAN", "LOB", "LOCK", "LOFT", "LOG", "LOGE", "LOIS",
    "LOLA", "LONE", "LONG", "LOOK", "LOON", "LOOT", "LOP", "LORD", "LORE",
    "LOS", "LOSE", "LOSS", "LOST", "LOT", "LOU", "LOUD", "LOVE", "LOW",
    "LOWE", "LOY", "LUCK", "LUCY", "LUG", "LUGE", "LUKE", "LULU", "LUND",
    "LUNG", "LURA", "LURE", "LURK", "LUSH", "LUST", "LYE", "LYLE", "LYNN",
    "LYON", "LYRA", "MA", "MAC", "MACE", "MAD", "MADE", "MAE", "MAGI",
    "MAID", "MAIL", "MAIN", "MAKE", "MALE", "MALI", "MALL", "MALT", "MAN",
    "MANA", "MANN", "MANY", "MAO", "MAP", "MARC", "MARE", "MARK", "MARS",
    "MART", "MARY", "MASH", "MASK", "MASS", "MAST", "MAT", "MATE", "MATH",
    "MAUL", "MAW", "MAY", "MAYO", "ME", "MEAD", "MEAL", "MEAN", "MEAT",
    "MEEK", "MEET", "MEG", "MEL", "MELD", "MELT", "MEMO", "MEN", "MEND",
    "MENU", "MERT", "MESH", "MESS", "MET", "MEW", "MICE", "MID", "MIKE",
    "MILD", "MILE", "MILK", "MILL", "MILT", "MIMI", "MIN", "MIND", "MINE",
    "MINI", "MINK", "MINT", "MIRE", "MISS", "MIST", "MIT", "MITE", "MITT",
    "MOAN", "MOAT", "MOB", "MOCK", "MOD", "MODE", "MOE", "MOLD", "MOLE",
    "MOLL", "MOLT", "MONA", "MONK", "MONT", "MOO", "MOOD", "MOON", "MOOR",
    "MOOT", "MOP", "MORE", "MORN", "MORT", "MOS", "MOSS", "MOST", "MOT",
    "MOTH", "MOVE", "MOW", "MUCH", "MUCK", "MUD", "MUDD", "MUFF", "MUG",
    "MULE", "MULL", "MUM", "MURK", "MUSH", "MUST", "MUTE", "MUTT", "MY",
    "MYRA", "MYTH", "NAB", "NAG", "NAGY", "NAIL", "NAIR", "NAME", "NAN",
    "NAP", "NARY", "NASH", "NAT", "NAVE", "NAVY", "NAY", "NE", "NEAL",
    "NEAR", "NEAT", "NECK", "NED", "NEE", "NEED", "NEIL", "NELL", "NEON",
    "NERO", "NESS", "NEST", "NET", "NEW", "NEWS", "NEWT", "NIB", "NIBS",
    "NICE", "NICK", "NIL", "NILE", "NINA", "NINE", "NIP", "NIT", "NO",
    "NOAH", "NOB", "NOD", "NODE", "NOEL", "NOLL", "NON", "NONE", "NOOK",
    "NOON", "NOR", "NORM", "NOSE", "NOT", "NOTE", "NOUN", "NOV", "NOVA",
    "NOW", "NU", "NUDE", "NULL", "NUMB", "NUN", "NUT", "O", "OAF", "OAK",
    "OAR", "OAT", "OATH", "OBEY", "OBOE", "ODD", "ODE", "ODIN", "OF",
    "OFF", "OFT", "OH", "OHIO", "OIL", "OILY", "OINT", "OK", "OKAY",
    "OLAF", "OLD", "OLDY", "OLGA", "OLIN", "OMAN", "OMEN", "OMIT", "ON",
    "ONCE", "ONE", "ONES", "ONLY", "ONTO", "ONUS", "OR", "ORAL", "ORB",
    "ORE", "ORGY", "ORR", "OS", "OSLO", "OTIS", "OTT", "OTTO", "OUCH",
    "OUR", "OUST", "OUT", "OUTS", "OVA", "OVAL", "OVEN", "OVER", "OW",
    "OWE", "OWL", "OWLY", "OWN", "OWNS", "OX", "PA", "PAD", "PAL", "PAM",
    "PAN", "PAP", "PAR", "PAT", "PAW", "PAY", "PEA", "PEG", "PEN", "PEP",
    "PER", "PET", "PEW", "PHI", "PI", "PIE", "PIN", "PIT", "PLY", "PO",
    "POD", "POE", "POP", "POT", "POW", "PRO", "PRY", "PUB", "PUG", "PUN",
    "PUP", "PUT", "QUAD", "QUIT", "QUO", "QUOD", "RACE", "RACK", "RACY",
    "RAFT", "RAG", "RAGE", "RAID", "RAIL", "RAIN", "RAKE", "RAM", "RAN",
    "RANK", "RANT", "RAP", "RARE", "RASH", "RAT", "RATE", "RAVE", "RAW",
    "RAY", "RAYS", "READ", "REAL", "REAM", "REAR", "REB", "RECK", "RED",
    "REED", "REEF", "REEK", "REEL", "REID", "REIN", "RENA", "REND",
    "RENT", "REP", "REST", "RET", "RIB", "RICE", "RICH", "RICK", "RID",
    "RIDE", "RIFT", "RIG", "RILL", "RIM", "RIME", "RING", "RINK", "RIO",
    "RIP", "RISE", "RISK", "RITE", "ROAD", "ROAM", "ROAR", "ROB", "ROBE",
    "ROCK", "ROD", "RODE", "ROE", "ROIL", "ROLL", "ROME", "RON", "ROOD",
    "ROOF", "ROOK", "ROOM", "ROOT", "ROSA", "ROSE", "ROSS", "ROSY", "ROT",
    "ROTH", "ROUT", "ROVE", "ROW", "ROWE", "ROWS", "ROY", "RUB", "RUBE",
    "RUBY", "RUDE", "RUDY", "RUE", "RUG", "RUIN", "RULE", "RUM", "RUN",
    "RUNG", "RUNS", "RUNT", "RUSE", "RUSH", "RUSK", "RUSS", "RUST",
    "RUTH", "RYE", "SAC", "SACK", "SAD", "SAFE", "SAG", "SAGE", "SAID",
    "SAIL", "SAL", "SALE", "SALK", "SALT", "SAM", "SAME", "SAN", "SAND",
    "SANE", "SANG", "SANK", "SAP", "SARA", "SAT", "SAUL", "SAVE", "SAW",
    "SAY", "SAYS", "SCAN", "SCAR", "SCAT", "SCOT", "SEA", "SEAL", "SEAM",
    "SEAR", "SEAT", "SEC", "SEE", "SEED", "SEEK", "SEEM", "SEEN", "SEES",
    "SELF", "SELL", "SEN", "SEND", "SENT", "SET", "SETS", "SEW", "SEWN",
    "SHAG", "SHAM", "SHAW", "SHAY", "SHE", "SHED", "SHIM", "SHIN", "SHOD",
    "SHOE", "SHOT", "SHOW", "SHUN", "SHUT", "SHY", "SICK", "SIDE", "SIFT",
    "SIGH", "SIGN", "SILK", "SILL", "SILO", "SILT", "SIN", "SINE", "SING",
    "SINK", "SIP", "SIR", "SIRE", "SIS", "SIT", "SITE", "SITS", "SITU",
    "SKAT", "SKEW", "SKI", "SKID", "SKIM", "SKIN", "SKIT", "SKY", "SLAB",
    "SLAM", "SLAT", "SLAY", "SLED", "SLEW", "SLID", "SLIM", "SLIT",
    "SLOB", "SLOG", "SLOT", "SLOW", "SLUG", "SLUM", "SLUR", "SLY", "SMOG",
    "SMUG", "SNAG", "SNOB", "SNOW", "SNUB", "SNUG", "SO", "SOAK", "SOAR",
    "SOB", "SOCK", "SOD", "SODA", "SOFA", "SOFT", "SOIL", "SOLD", "SOME",
    "SON", "SONG", "SOON", "SOOT", "SOP", "SORE", "SORT", "SOUL", "SOUR",
    "SOW", "SOWN", "SOY", "SPA", "SPY", "STAB", "STAG", "STAN", "STAR",
    "STAY", "STEM", "STEW", "STIR", "STOW", "STUB", "STUN", "SUB", "SUCH",
    "SUD", "SUDS", "SUE", "SUIT", "SULK", "SUM", "SUMS", "SUN", "SUNG",
    "SUNK", "SUP", "SURE", "SURF", "SWAB", "SWAG", "SWAM", "SWAN", "SWAT",
    "SWAY", "SWIM", "SWUM", "TAB", "TACK", "TACT", "TAD", "TAG", "TAIL",
    "TAKE", "TALE", "TALK", "TALL", "TAN", "TANK", "TAP", "TAR", "TASK",
    "TATE", "TAUT", "TEA", "TEAL", "TEAM", "TEAR", "TECH", "TED", "TEE",
    "TEEM", "TEEN", "TEET", "TELL", "TEN", "TEND", "TENT", "TERM", "TERN",
    "TESS", "TEST", "THAN", "THAT", "THE", "THEE", "THEM", "THEN", "THEY",
    "THIN", "THIS", "THUD", "THUG", "THY", "TIC", "TICK", "TIDE", "TIDY",
    "TIE", "TIED", "TIER", "TILE", "TILL", "TILT", "TIM", "TIME", "TIN",
    "TINA", "TINE", "TINT", "TINY", "TIP", "TIRE", "TO", "TOAD", "TOE",
    "TOG", "TOGO", "TOIL", "TOLD", "TOLL", "TOM", "TON", "TONE", "TONG",
    "TONY", "TOO", "TOOK", "TOOL", "TOOT", "TOP", "TORE", "TORN", "TOTE",
    "TOUR", "TOUT", "TOW", "TOWN", "TOY", "TRAG", "TRAM", "TRAY", "TREE",
    "TREK", "TRIG", "TRIM", "TRIO", "TROD", "TROT", "TROY", "TRUE", "TRY",
    "TUB", "TUBA", "TUBE", "TUCK", "TUFT", "TUG", "TUM", "TUN", "TUNA",
    "TUNE", "TUNG", "TURF", "TURN", "TUSK", "TWIG", "TWIN", "TWIT", "TWO",
    "ULAN", "UN", "UNIT", "UP", "URGE", "US", "USE", "USED", "USER",
    "USES", "UTAH", "VAIL", "VAIN", "VALE", "VAN", "VARY", "VASE", "VAST",
    "VAT", "VEAL", "VEDA", "VEIL", "VEIN", "VEND", "VENT", "VERB", "VERY",
    "VET", "VETO", "VICE", "VIE", "VIEW", "VINE", "VISE", "VOID", "VOLT",
    "VOTE", "WACK", "WAD", "WADE", "WAG", "WAGE", "WAIL", "WAIT", "WAKE",
    "WALE", "WALK", "WALL", "WALT", "WAND", "WANE", "WANG", "WANT", "WAR",
    "WARD", "WARM", "WARN", "WART", "WAS", "WASH", "WAST", "WATS", "WATT",
    "WAVE", "WAVY", "WAY", "WAYS", "WE", "WEAK", "WEAL", "WEAN", "WEAR",
    "WEB", "WED", "WEE", "WEED", "WEEK", "WEIR", "WELD", "WELL", "WELT",
    "WENT", "WERE", "WERT", "WEST", "WET", "WHAM", "WHAT", "WHEE", "WHEN",
    "WHET", "WHO", "WHOA", "WHOM", "WHY", "WICK", "WIFE", "WILD", "WILL",
    "WIN", "WIND", "WINE", "WING", "WINK", "WINO", "WIRE", "WISE", "WISH",
    "WIT", "WITH", "WOK", "WOLF", "WON", "WONT", "WOO", "WOOD", "WOOL",
    "WORD", "WORE", "WORK", "WORM", "WORN", "WOVE", "WOW", "WRIT", "WRY",
    "WU", "WYNN", "YALE", "YAM", "YANG", "YANK", "YAP", "YARD", "YARN",
    "YAW", "YAWL", "YAWN", "YE", "YEA", "YEAH", "YEAR", "YELL", "YES",
    "YET", "YOGA", "YOKE", "YOU" };

  public static void main(String [] args) {
    for (int i = 0; i < 10; i++) {
      String account = generateAccountId();
      String code = accountToCode(account);
      String verify = codeToAccount(code);
      if (verify.equals(account)) {
        System.out.println("Test pass: AccountId = " + account +
          " Code = " + code);
      } else {
        System.out.println("TEST FAILED! AccountId = " + account +
          " Code = " + code + " Verifier = " + verify);
      }
    }
  }

}

