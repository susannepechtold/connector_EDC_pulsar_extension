/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.spi.monitor;

public interface ConsoleColor {
    // Reset
    String RESET = "\033[0m";

    // Regular Colors
    String BLACK = "\033[0;30m";
    String RED = "\033[0;31m";
    String GREEN = "\033[0;32m";
    String YELLOW = "\033[0;33m";
    String BLUE = "\033[0;34m";
    String PURPLE = "\033[0;35m";
    String CYAN = "\033[0;36m";
    String WHITE = "\033[0;37m";

    // Bold
    String BLACK_BOLD = "\033[1;30m";
    String RED_BOLD = "\033[1;31m";
    String GREEN_BOLD = "\033[1;32m";
    String YELLOW_BOLD = "\033[1;33m";
    String BLUE_BOLD = "\033[1;34m";
    String PURPLE_BOLD = "\033[1;35m";
    String CYAN_BOLD = "\033[1;36m";
    String WHITE_BOLD = "\033[1;37m";

    // Underline
    String BLACK_UNDERLINED = "\033[4;30m";
    String RED_UNDERLINED = "\033[4;31m";
    String GREEN_UNDERLINED = "\033[4;32m";
    String YELLOW_UNDERLINED = "\033[4;33m";
    String BLUE_UNDERLINED = "\033[4;34m";
    String PURPLE_UNDERLINED = "\033[4;35m";
    String CYAN_UNDERLINED = "\033[4;36m";
    String WHITE_UNDERLINED = "\033[4;37m";

    // Background
    String BLACK_BACKGROUND = "\033[40m";
    String RED_BACKGROUND = "\033[41m";
    String GREEN_BACKGROUND = "\033[42m";
    String YELLOW_BACKGROUND = "\033[43m";
    String BLUE_BACKGROUND = "\033[44m";
    String PURPLE_BACKGROUND = "\033[45m";
    String CYAN_BACKGROUND = "\033[46m";
    String WHITE_BACKGROUND = "\033[47m";

    // High Intensity
    String BLACK_BRIGHT = "\033[0;90m";
    String RED_BRIGHT = "\033[0;91m";
    String GREEN_BRIGHT = "\033[0;92m";
    String YELLOW_BRIGHT = "\033[0;93m";
    String BLUE_BRIGHT = "\033[0;94m";
    String PURPLE_BRIGHT = "\033[0;95m";
    String CYAN_BRIGHT = "\033[0;96m";
    String WHITE_BRIGHT = "\033[0;97m";

    // Bold High Intensity
    String BLACK_BOLD_BRIGHT = "\033[1;90m";
    String RED_BOLD_BRIGHT = "\033[1;91m";
    String GREEN_BOLD_BRIGHT = "\033[1;92m";
    String YELLOW_BOLD_BRIGHT = "\033[1;93m";
    String BLUE_BOLD_BRIGHT = "\033[1;94m";
    String PURPLE_BOLD_BRIGHT = "\033[1;95m";
    String CYAN_BOLD_BRIGHT = "\033[1;96m";
    String WHITE_BOLD_BRIGHT = "\033[1;97m";

    // High Intensity backgrounds
    String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";
    String RED_BACKGROUND_BRIGHT = "\033[0;101m";
    String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";
    String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";
    String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";
    String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m";
    String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";
    String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";
}