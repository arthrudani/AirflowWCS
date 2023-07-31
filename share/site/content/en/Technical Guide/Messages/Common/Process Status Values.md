---
title: "Process Status Values"
linkTitle: "Process Status Values"
weight: 2
type: docs
simple_list: true

---

# Process Status Values

|bit |Description | Comments |
|---|------------|----|
| 0 |Unknown / Not Available  | Unknown or Not Available status|
| 1 |Processing  | Currently executing |
| 2 |Processed / Successed | The process is completed successfully |
| 3 |Processed (Shortage) | The process is completed successfully but there are shortages |
| 4 |Failed | Failed to complete the process|
| 5 |Error | Exception occurred during the process|
| 6 |Bin Full Error|When an unexpected bag is found in the location |
| 7 |Bin Empty Error|When there is no bag in the location which was expected  |



