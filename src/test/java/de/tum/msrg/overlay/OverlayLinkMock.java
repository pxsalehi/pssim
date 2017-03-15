package de.tum.msrg.overlay;

/**
 * Created by pxsalehi on 22.04.16.
 */
public class OverlayLinkMock extends OverlayLink {
    private int latency;
    private int fromNode;
    private int toNode;

    public OverlayLinkMock(int fromNode, int toNode, int latency) {
        this.fromNode = fromNode;
        this.latency = latency;
        this.toNode = toNode;
    }

    @Override
    public int getLatency() {
        return latency;
    }

    @Override
    public int hashCode() {
        int result = fromNode;
        result = 31 * result + toNode;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OverlayLinkMock that = (OverlayLinkMock) o;
        if (fromNode != that.fromNode) return false;
        return toNode == that.toNode;

    }

    @Override
    public String toString() {
        return "(link:" + fromNode + "-" + toNode + ")";
    }

}
