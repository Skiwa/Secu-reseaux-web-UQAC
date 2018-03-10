#!/bin/bash

# The goal of this iptables.sh is to block ABSOLUTELY EVERYTHING except
# our outgoing encrypted messages to rpiexplorer.io:8080
# our incoming encrpyted messages from rpiexplorer.io to our server's (unknown ip yet) port 8080
# a DNS connection
# Also, this includes some kind of DDoS protection.

# 1. Delete all existing rules, chains, nat and mangle
iptables -F
iptables -X
iptables -t nat -F
iptables -t nat -X
iptables -t mangle -F
iptables -t mangle -X

# 2. Set default chain policies to block everything
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT DROP

# 3. Block all invalid packets and all NON-SYN packets.
iptables -t mangle -A PREROUTING -m conntrack --ctstate INVALID -j DROP 
iptables -t mangle -A PREROUTING -p tcp ! --syn -m conntrack --ctstate NEW -j DROP

# 4. Drop SYN packets with suspicious MSS value
iptables -t mangle -A PREROUTING -p tcp -m conntrack --ctstate NEW -m tcpmss ! --mss 536:65535 -j DROP

# 5. Block packets with flags that legit TCP connections wouldn't use
iptables -t mangle -A PREROUTING -p tcp --tcp-flags FIN,SYN,RST,PSH,ACK,URG NONE -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags FIN,SYN FIN,SYN -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags SYN,RST SYN,RST -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags FIN,RST FIN,RST -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags FIN,ACK FIN -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ACK,URG URG -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ACK,FIN FIN -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ACK,PSH PSH -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ALL ALL -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ALL NONE -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ALL FIN,PSH,URG -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ALL SYN,FIN,PSH,URG -j DROP 
iptables -t mangle -A PREROUTING -p tcp --tcp-flags ALL SYN,RST,ACK,FIN,URG -j DROP

# 6. Imposes a limit of 100 connections per IP. We will raise it if an issue occurs during the message transfer.
iptables -A INPUT -p tcp -m connlimit --connlimit-above 100 -j REJECT --reject-with tcp-reset 

# 7. Rules for reponses
iptables -I INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT
iptables -I OUTPUT -m state --state RELATED,ESTABLISHED -j ACCEPT

# 8. Rules for DNS
iptables -A OUTPUT -p udp --dport 53 -m state --state NEW,ESTABLISHED -j ACCEPT

# 9. Add rule to accept all Outgoing connections to rpiexplorer.io:8080
iptables -A OUTPUT -p tcp -d rpiexplorer.io --dport 8080 -m state --state NEW,ESTABLISHED -j ACCEPT

# 10. Add rule to accept all Incoming connections from rpiexplorer.io to our port 8080
iptables -A INPUT -p tcp -s rpiexplorer.io --dport 8080 -m state --state NEW,ESTABLISHED -j ACCEPT

