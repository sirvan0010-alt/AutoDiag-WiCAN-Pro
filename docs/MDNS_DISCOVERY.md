# WiCAN mDNS / Android NSD Discovery

AutoDiag uses Android `NsdManager` to discover local WiCAN-compatible network services. Discovery is only an endpoint-discovery mechanism; it does not prove that the adapter, firmware or vehicle supports a diagnostic capability.

## Preferred flow

1. Start mDNS discovery.
2. Resolve the discovered service to an IP address and advertised port.
3. Offer the WiCAN ELM327 endpoint on TCP `3333` and raw/SLCAN endpoint on TCP `23` where appropriate.
4. Perform a transport handshake.
5. Run Capability Discovery only after a connection is established.

A manually entered IP address must always remain available.

## When discovery returns no devices

The application must not claim that WiCAN is out of range. A common cause is client/AP isolation on the Wi-Fi network.

Typical router names include:

- AP Isolation
- Client Isolation
- Station Isolation
- Wireless Isolation
- WLAN Partition

With client isolation enabled, the phone and WiCAN can both have internet access while being prevented from opening connections to each other. mDNS and TCP can therefore fail even though the Wi-Fi icon looks normal.

### User-facing recovery flow

After a discovery timeout (target: 8–12 seconds), show:

> **Zařízení nebylo nalezeno.**
>
> Telefon a WiCAN musí být ve stejné síti a síť nesmí blokovat komunikaci mezi zařízeními. Častou příčinou je AP/Client Isolation.

Offer:

- **Zadat IP ručně**
- **Použít WiCAN v režimu Access Point**
- **Jak vypnout izolaci Wi-Fi?**
- **Zkusit hledání znovu**

The application must not silently change the user's Wi-Fi network.

## WiCAN Access Point fallback

WiCAN's own AP mode can bypass a problematic home router because the phone communicates directly with the adapter. The trade-off is that the phone may temporarily lose internet access on that Wi-Fi connection.

## Other limitations

- Guest Wi-Fi frequently enforces client isolation and may not allow local device-to-device traffic.
- Some networks filter multicast/mDNS even without full client isolation.
- Different WiCAN firmware versions may advertise different service types. `_http._tcp` is a useful initial discovery target; `_wican._tcp` can be added when confirmed by the firmware.
- Discovery does not replace a transport handshake or capability probe.

## Safety / privacy

AutoDiag should not perform a broad network scan silently. Any optional local subnet probe must be explicitly user initiated, rate limited and cancellable. It is a connectivity fallback, not a method of bypassing network isolation.
