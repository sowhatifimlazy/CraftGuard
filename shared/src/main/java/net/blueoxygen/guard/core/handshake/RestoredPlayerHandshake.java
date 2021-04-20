package net.blueoxygen.guard.core.handshake;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class RestoredPlayerHandshake {

    private final String originalAddress;
    private final String playerIp;
    private final int playerPort;

}
