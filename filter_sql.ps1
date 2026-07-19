$content = Get-Content "D:\Desktop\shopapp\shopapp-backend\database.sql" -Encoding UTF8

$filtered = $content | Where-Object {
    $line = $_.Trim()
    -not ($line -match "DROP DATABASE" -or
          $line -match "CREATE DATABASE" -or
          $line -match "USE ShopApp" -or
          $line -match "USE ``ShopApp``")
}

$filtered | Set-Content "D:\Desktop\ecom\ecom_be\ecom_schema.sql" -Encoding UTF8
Write-Host "Done. Lines: $($filtered.Count)"
