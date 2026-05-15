# Makefile for the CICS Simulator Runtime Library
#
# Builds libcicssim.so - the shared library that provides CICS API
# implementations for GnuCOBOL programs preprocessed by preprocess.py.
#
# Prerequisites:
#   - GCC
#   - libdb-dev (Berkeley DB development headers)
#   - libncurses-dev (optional, for full-screen terminal mode)

CC = gcc
CFLAGS = -fPIC -Wall -Wextra -O2 -I/usr/include
LDFLAGS = -shared
LIBS = -ldb

# Optional ncurses support
NCURSES_CFLAGS = -DUSE_NCURSES
NCURSES_LIBS = -lncurses

# Output
LIB_NAME = libcicssim.so
INSTALL_DIR = /usr/local/lib/gnucobol

# Source files
SRCS = cics_runtime.c cics_file.c cics_terminal.c cics_cobol_interface.c
OBJS = $(SRCS:.c=.o)

.PHONY: all clean install ncurses

# Default: build without ncurses (text-mode terminal)
all: $(LIB_NAME)

# Build with ncurses support
ncurses: CFLAGS += $(NCURSES_CFLAGS)
ncurses: LIBS += $(NCURSES_LIBS)
ncurses: $(LIB_NAME)

$(LIB_NAME): $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $(OBJS) $(LIBS)
	@echo "Built: $@"

%.o: %.c cics_runtime.h
	$(CC) $(CFLAGS) -c -o $@ $<

# Build with map registrations included
maps: cics_maps_generated.o $(OBJS)
	$(CC) $(LDFLAGS) -o $(LIB_NAME) $(OBJS) cics_maps_generated.o $(LIBS)

cics_maps_generated.o: cics_maps_generated.c cics_runtime.h
	$(CC) $(CFLAGS) -c -o $@ $<

install: $(LIB_NAME)
	sudo cp $(LIB_NAME) $(INSTALL_DIR)/
	sudo ldconfig
	@echo "Installed: $(INSTALL_DIR)/$(LIB_NAME)"

clean:
	rm -f $(OBJS) $(LIB_NAME) cics_maps_generated.o
