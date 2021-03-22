# Debug

This file contains common error messages that would be thrown if your input has problems.

## Crashes on start

### Symptons
- XSetErrorHandler() called with a GDK error trap pushed. Don't do that
- [xcb] Unknown sequence number while processing queue
### FIX 
- -Djdk.gtk.version=2