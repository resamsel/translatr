#!/bin/bash

I=$1

translatr user ls
translatr project create P$I
translatr project rm P$I
