[Setup]
AppName={#ApplicationFullName}
AppVersion={#ApplicationVersion}
AppVerName={#ApplicationFullName} {#ApplicationVersion}
AppPublisher={#ApplicationCompany}
AppId={#ApplicationUUID}
AppCopyright={#ApplicationCopyright}
AppPublisherURL={#ApplicationURL}
AppSupportURL={#ApplicationURL}
AppUpdatesURL={#ApplicationURL}
SetupIconFile=IconSetup.ico
WizardImageFile=Image.bmp
WizardSmallImageFile=SmallImage.bmp
UninstallDisplayIcon={app}\{#ApplicationName}.exe

DefaultDirName={pf}\{#ApplicationFullName}
DefaultGroupName={#ApplicationFullName}
MinVersion=6.0

OutputDir=output
OutputBaseFilename=setup
Compression=lzma2/ultra64
SolidCompression=yes
AllowNoIcons=yes
LicenseFile=License.txt
ArchitecturesInstallIn64BitMode=x64

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"

[Files]
Source: "install\common\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs solidbreak
Source: "install\32\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs solidbreak; Check: not Is64BitInstallMode
Source: "install\64\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs solidbreak; Check: Is64BitInstallMode

[Icons]
Name: "{group}\{#ApplicationFullName}"; Filename: "{app}\{#ApplicationName}"
Name: "{commondesktop}\{#ApplicationFullName}"; Filename: "{app}\{#ApplicationName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#ApplicationName}"; Description: "{cm:LaunchProgram,{#StringChange(ApplicationFullName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[InstallDelete]
Type: filesandordirs; Name: "{app}\lib\*";
Type: filesandordirs; Name: "{app}\jre\*";
