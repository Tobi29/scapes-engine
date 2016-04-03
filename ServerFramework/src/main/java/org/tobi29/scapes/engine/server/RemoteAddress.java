package org.tobi29.scapes.engine.server;

import org.tobi29.scapes.engine.utils.io.tag.MultiTag;
import org.tobi29.scapes.engine.utils.io.tag.TagStructure;

import java.net.InetSocketAddress;

public class RemoteAddress implements MultiTag.Writeable {
    public final String address;
    public final int port;

    public RemoteAddress(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public RemoteAddress(InetSocketAddress address) {
        this.address = address.getHostString();
        port = address.getPort();
    }

    public RemoteAddress(TagStructure tagStructure) {
        address = tagStructure.getString("Address");
        port = tagStructure.getInteger("Port");
    }

    @Override
    public TagStructure write() {
        TagStructure tagStructure = new TagStructure();
        tagStructure.setString("Address", address);
        tagStructure.setInteger("Port", port);
        return tagStructure;
    }
}
