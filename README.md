# DeleteInstance

This program is specifically made to be show the issue i have with deleting instances of
repeating events from googles calendar contract.

How this works is when the program is run it shows a start button.
Pressing start creats a new calendar using the account details set in the
CalHandler class.

It then creates a repeating event and then tries to delete the
first instance. But as is shown the first instance is actually left alone
and the other instances are deleted.
