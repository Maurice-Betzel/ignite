/*
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

namespace php org.apache.ignite.thrift.protocol
namespace java org.apache.ignite.thrift.protocol

const string PROTOCOL_VERSION = "1.8.0.0"
const string APPLICATION_XTHRIFT = "application/x-thrift"
const i16 SUCCESS = 0
const i16 FAILED = 1
const i16 AUTHORIZATION_FAILED = 2
const i16 SECURITY_CHECK_FAILED = 3

enum GridClientPacketType {
    MEMCACHE, THRIFT, IGNITE_HANDSHAKE, IGNITE_HANDSHAKE_RES, IGNITE
}

enum GridCacheCommand {
    DESTROY_CACHE,
    GET_OR_CREATE_CACHE,
    CACHE_CONTAINS_KEYS,
    CACHE_CONTAINS_KEY,
    CACHE_GET,
    CACHE_GET_AND_PUT,
    CACHE_GET_AND_REPLACE,
    CACHE_GET_AND_PUT_IF_ABSENT,
    CACHE_PUT_IF_ABSENT,
    CACHE_GET_ALL,
    CACHE_PUT,
    CACHE_ADD,
    CACHE_PUT_ALL,
    CACHE_REMOVE,
    CACHE_REMOVE_VALUE,
    CACHE_REPLACE_VALUE,
    CACHE_GET_AND_REMOVE,
    CACHE_REMOVE_ALL,
    CACHE_REPLACE,
    CACHE_CAS,
    CACHE_APPEND,
    CACHE_PREPEND,
    CACHE_METRICS,
    CACHE_SIZE,
    CACHE_METADATA
}

enum GridDataStructureCommand {
    ATOMIC_INCREMENT, ATOMIC_DECREMENT
}

enum GridLogCommand {
    LOG
}

enum GridQueryCommand {
    EXECUTE_SQL_QUERY, EXECUTE_SQL_FIELDS_QUERY, EXECUTE_SCAN_QUERY, FETCH_SQL_QUERY, CLOSE_SQL_QUERY
}

enum GridTaskCommand {
    EXE, RESULT, NOOP
}

enum GridTopologyCommand {
    TOPOLOGY, NODE
}

enum GridVersionCommand {
    VERSION, NAME
}

exception IgniteException {
    1: required string type,
    2: required string message,
    3: optional string trace
}

struct IgniteResponse {
    1: required string affinityNodeId,
    2: required binary response,
    3: required i16 successStatus,
    4: optional IgniteException error
}

struct IgniteRequest {
    1: required string command
}