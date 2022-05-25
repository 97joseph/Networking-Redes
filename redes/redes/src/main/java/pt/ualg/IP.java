package pt.ualg;

/**
 * The class IP represents an IP address.
 *
 * @author Mar
 */
public class IP {
    private long value;
    private static final int BITS_PER_SECTION = 8;
    private static final int SECTIONS = 4;

    private static final long FULL_MASK = 0b11111111111111111111111111111111;

    private IP(long value) {
        this.value = value&FULL_MASK;
    }

    /**
     * Creates an IP address from a string.
     *
     * @param ipString
     *            The string representation of the IP address.
     * @return The IP address.
     */
    public IP(String value) {
        String[] parts = value.split("\\.");
        if (parts.length != SECTIONS)
            throw new IllegalArgumentException("Invalid IP " + value);
        this.value = 0;
        for (int i = 0; i < SECTIONS; i++) {
            long part = Long.parseLong(parts[i]);
            if (part >= (1<<BITS_PER_SECTION) || part < 0)
                throw new IllegalArgumentException("Invalid IP section " + part);
            this.value |= part << ((SECTIONS-1-i)*BITS_PER_SECTION);
        }
    }

    private static long prefixToMask(int prefix) {
        if (prefix > 32 || prefix < 0)
            throw new IllegalArgumentException("Invalid CIDR prefix " + prefix);
        return FULL_MASK<<(32-prefix)&FULL_MASK;
    }

    /**
     * An IP address Block, representing the range between two IP addresses.
     */
    public static class Block {
        private IP start;
        private IP end;

        /**
         * Creates a new IP address block.
         *
         * @param start
         *            The start of the block.
         * @param prefix
         *            The prefix of the block.
         */
        public Block(IP start, int mask) {
            this.start = mask(start, mask);
            this.end = maskEnd(start, mask);
        }

        /**
         * Whether a block contains a given IP address.
         * @param ip The IP address.
         * @return true if the block contains the IP address, false otherwise.
         */
        public boolean contains(IP ip) {
            return start.value <= ip.value && end.value >= ip.value;
        }

        private static IP mask(IP ip, int prefix) {
            long mask = prefixToMask(prefix);
            return new IP(ip.value&mask);
        }

        private static IP maskEnd(IP ip, int prefix) {
            long mask = prefixToMask(prefix);
            return new IP(ip.value&mask | (~mask)&FULL_MASK);
        }

        @Override
        public String toString() {
            return start + " - " + end;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof IP.Block))
                return false;
            IP.Block that = (IP.Block) obj;
            return this.start.equals(that.start) && this.end.equals(that.end);
        }

        @Override
        public int hashCode() {
            return start.hashCode()*31 + end.hashCode();
        }
    }

    @Override
    public String toString() {
        int mask = 0B11111111<<((SECTIONS-1)*BITS_PER_SECTION);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < SECTIONS-1; i++) {
            result.append(( (value&mask) >>> (BITS_PER_SECTION*(SECTIONS-1-i)) ) + ".");
            mask >>>= BITS_PER_SECTION;
        }
        result.append(value&mask);
        return result.toString();
    }

    @Override
    public boolean equals(Object arg0) {
        if (this==arg0)
            return true;
        if (!(arg0 instanceof IP))
            return false;
        IP that = (IP) arg0;
        return this.value == that.value;
    }

    @Override
    public int hashCode() {
        return (int) this.value;
    }
}
