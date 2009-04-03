Dim fso, d, dc, s

   Set fso = CreateObject("Scripting.FileSystemObject")
   Set dc = fso.Drives
   For Each d in dc
      s = s & d.DriveLetter & ":" & d.DriveType & vbCrLf 
   Next

Dim file
Set file = fso.CreateTextFile("drives", True)
file.Write(s)
file.Close